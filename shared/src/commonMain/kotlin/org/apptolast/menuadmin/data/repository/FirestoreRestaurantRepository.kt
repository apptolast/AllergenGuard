package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument
import org.apptolast.menuadmin.domain.model.AccountSession
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.repository.RestaurantRepository

/**
 * [RestaurantRepository] backed by the top-level Firestore `restaurants` collection. Document id =
 * restaurant slug (matches the seeded data and keeps recipe/menu subcollection paths stable).
 *
 * Restaurants stay top-level because the consumer app reads them globally (a public marketplace). The
 * admin scopes them to the signed-in account **client-side** (the REST client has no server-side query):
 * only restaurants whose `accountId` matches the session, and for a RESTAURANT_MANAGER only the ones
 * assigned in their membership.
 *
 * Writes use an [updateMask] so admin-owned fields are set without clobbering server/MVP fields the
 * admin model doesn't carry (`rating`, `location`, `openingHours`). `accountId` is stamped from the
 * current session so the security rules accept the write.
 */
class FirestoreRestaurantRepository(
    private val firestore: FirestoreClient,
    private val accountHolder: CurrentAccountHolder,
) : RestaurantRepository {
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    private var hasLoaded = false

    // This repo is a singleton, so its cache outlives a logout. Remember which session the cache was
    // built for and reload when it changes (a different user signing in on the same browser, or an
    // admin↔manager switch) — otherwise the new user would see the previous user's restaurants. The
    // whole session is compared, not just the account, because role/restaurantIds change the filter.
    private var loadedSession: AccountSession? = null

    override fun getAllRestaurants(): Flow<List<Restaurant>> =
        flow {
            if (!hasLoaded || loadedSession != accountHolder.session.value) {
                runCatching { refresh() }
            }
            emitAll(_restaurants)
        }

    private suspend fun refresh() {
        val session = accountHolder.session.value
        loadedSession = session
        if (session == null) {
            _restaurants.value = emptyList()
            hasLoaded = true
            return
        }
        _restaurants.value = firestore.listDocuments(COLLECTION)
            .filter { (it.fields["accountId"] as? String) == session.accountId && session.canSeeRestaurant(it.id) }
            .map { it.toRestaurant() }
        hasLoaded = true
    }

    override suspend fun getRestaurantById(id: String): Restaurant? =
        firestore.getDocument("$COLLECTION/$id")?.toRestaurant()

    override suspend fun createRestaurant(restaurant: Restaurant): Restaurant {
        val id = restaurant.slug.ifEmpty { restaurant.id }
        firestore.patchDocument("$COLLECTION/$id", restaurant.toFields(), updateMask = FIELD_MASK)
        refresh()
        return _restaurants.value.find { it.id == id } ?: restaurant.copy(id = id)
    }

    override suspend fun updateRestaurant(restaurant: Restaurant): Restaurant {
        firestore.patchDocument("$COLLECTION/${restaurant.id}", restaurant.toFields(), updateMask = FIELD_MASK)
        refresh()
        return _restaurants.value.find { it.id == restaurant.id } ?: restaurant
    }

    override suspend fun deleteRestaurant(id: String) {
        firestore.deleteDocument("$COLLECTION/$id")
        refresh()
    }

    private fun FirestoreDocument.toRestaurant(): Restaurant =
        Restaurant(
            id = id,
            name = fields["name"] as? String ?: "",
            slug = fields["slug"] as? String ?: id,
            description = fields["description"] as? String ?: "",
            address = fields["address"] as? String ?: "",
            phone = fields["phone"] as? String ?: "",
            logoUrl = fields["logoUrl"] as? String,
            active = fields["active"] as? Boolean ?: true,
        )

    private fun Restaurant.toFields(): Map<String, Any?> =
        mapOf(
            "name" to name,
            "slug" to slug,
            "description" to description,
            "address" to address,
            "phone" to phone,
            "logoUrl" to logoUrl,
            "active" to active,
            "accountId" to accountHolder.requireAccountId(),
        )

    private companion object {
        const val COLLECTION = "restaurants"
        val FIELD_MASK = listOf("name", "slug", "description", "address", "phone", "logoUrl", "active", "accountId")
    }
}
