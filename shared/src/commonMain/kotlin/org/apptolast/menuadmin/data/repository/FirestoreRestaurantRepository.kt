package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.firebase.FirebaseIdToken
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.repository.RestaurantRepository

/**
 * [RestaurantRepository] backed by the top-level Firestore `restaurants` collection. Document id =
 * restaurant slug (matches the seeded data and keeps recipe/menu subcollection paths stable).
 *
 * Writes use an [updateMask] so admin-owned fields are set without clobbering server/MVP fields the
 * admin model doesn't carry (`rating`, `location`, `openingHours`). `accountId` is taken from the
 * signed-in user's token claim so the security rules accept the write.
 */
class FirestoreRestaurantRepository(
    private val firestore: FirestoreClient,
    private val tokenManager: TokenManager,
) : RestaurantRepository {
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    private var hasLoaded = false

    override fun getAllRestaurants(): Flow<List<Restaurant>> =
        flow {
            if (!hasLoaded) runCatching { refresh() }
            emitAll(_restaurants)
        }

    private suspend fun refresh() {
        _restaurants.value = firestore.listDocuments(COLLECTION).map { it.toRestaurant() }
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
            "accountId" to (FirebaseIdToken.claim(tokenManager.accessToken, "accountId") ?: DEFAULT_ACCOUNT),
        )

    private companion object {
        const val COLLECTION = "restaurants"
        const val DEFAULT_ACCOUNT = "acc_apptolast"
        val FIELD_MASK = listOf("name", "slug", "description", "address", "phone", "logoUrl", "active", "accountId")
    }
}
