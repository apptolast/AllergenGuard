package com.apptolast.menufrontend.data.firebase

import com.apptolast.menufrontend.data.repository.RestaurantRepository
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.domain.model.Restaurant
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient

/**
 * Consumer [RestaurantRepository] backed by the shared Firestore data. Restaurants are read from the
 * public `restaurants` collection; the consumer's "dishes" are the restaurant's `recipes`.
 */
class FirestoreRestaurantRepository(
    private val firestore: FirestoreClient,
) : RestaurantRepository {
    // dishId -> dish (cached when a restaurant menu is loaded, so getDishDetail can resolve it).
    private val dishCache = mutableMapOf<String, Dish>()

    override suspend fun getNearbyRestaurants(): Result<List<Restaurant>> = runCatching {
        firestore.listDocuments(COLLECTION).map { it.toRestaurant() }
    }

    override suspend fun searchRestaurants(query: String): Result<List<Restaurant>> = runCatching {
        firestore.listDocuments(COLLECTION).map { it.toRestaurant() }.filter {
            it.name.contains(query, ignoreCase = true) || it.cuisineType.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getRestaurantMenu(restaurantId: String): Result<List<Dish>> = runCatching {
        val dishes = firestore.listDocuments("$COLLECTION/$restaurantId/recipes").map { it.toDish(restaurantId) }
        dishes.forEach { dishCache[it.id] = it }
        dishes
    }

    override suspend fun getDishDetail(dishId: String): Result<Dish> = runCatching {
        dishCache[dishId] ?: throw NoSuchElementException("Dish $dishId not loaded")
    }

    override suspend fun getRestaurantName(restaurantId: String): Result<String> = runCatching {
        firestore.getDocument("$COLLECTION/$restaurantId")?.fields?.get("name") as? String ?: ""
    }

    private companion object {
        const val COLLECTION = "restaurants"
    }
}
