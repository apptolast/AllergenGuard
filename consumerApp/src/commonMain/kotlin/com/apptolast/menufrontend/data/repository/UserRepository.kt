package com.apptolast.menufrontend.data.repository

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Restaurant

interface UserRepository {
    suspend fun getUserAllergens(): Result<Set<Allergen>>
    suspend fun updateUserAllergens(allergens: Set<Allergen>): Result<Unit>
    suspend fun getFavoriteRestaurants(): Result<List<Restaurant>>
    suspend fun toggleFavorite(restaurantId: String): Result<Unit>
}
