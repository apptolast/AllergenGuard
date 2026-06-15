package com.apptolast.menufrontend.data.firebase

import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Restaurant
import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.firebase.FirebaseIdToken
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient

/**
 * Consumer [UserRepository] backed by the private `users/{uid}` document in Firestore (allergen
 * profile + favorites). The uid comes from the signed-in user's Firebase ID token claim.
 */
class FirestoreUserRepository(
    private val firestore: FirestoreClient,
    private val tokenManager: TokenManager,
) : UserRepository {
    private fun uid(): String? =
        FirebaseIdToken.claim(tokenManager.accessToken, "user_id")
            ?: FirebaseIdToken.claim(tokenManager.accessToken, "sub")

    override suspend fun getUserAllergens(): Result<Set<Allergen>> = runCatching {
        val uid = uid() ?: return@runCatching emptySet()
        val doc = firestore.getDocument("users/$uid") ?: return@runCatching emptySet()
        @Suppress("UNCHECKED_CAST")
        val profile = doc.fields["allergenProfile"] as? Map<String, Any?>
        (profile?.get("codes") as? List<Any?>).orEmpty().filterIsInstance<String>()
            .mapNotNull(::allergenFromCode).toSet()
    }

    override suspend fun updateUserAllergens(allergens: Set<Allergen>): Result<Unit> = runCatching {
        val uid = uid() ?: throw IllegalStateException("Not logged in")
        val profile = mapOf("codes" to allergens.map { it.toApiCode() }, "notes" to "")
        firestore.patchDocument("users/$uid", mapOf("allergenProfile" to profile), updateMask = listOf("allergenProfile"))
        Unit
    }

    override suspend fun getFavoriteRestaurants(): Result<List<Restaurant>> = runCatching {
        val uid = uid() ?: return@runCatching emptyList()
        val doc = firestore.getDocument("users/$uid") ?: return@runCatching emptyList()
        (doc.fields["favorites"] as? List<Any?>).orEmpty().filterIsInstance<String>()
            .mapNotNull { rid -> firestore.getDocument("restaurants/$rid")?.toRestaurant() }
    }

    override suspend fun toggleFavorite(restaurantId: String): Result<Unit> = runCatching {
        val uid = uid() ?: throw IllegalStateException("Not logged in")
        val doc = firestore.getDocument("users/$uid")
        val favorites = (doc?.fields?.get("favorites") as? List<Any?>).orEmpty().filterIsInstance<String>().toMutableSet()
        if (!favorites.add(restaurantId)) favorites.remove(restaurantId)
        firestore.patchDocument("users/$uid", mapOf("favorites" to favorites.toList()), updateMask = listOf("favorites"))
        Unit
    }
}
