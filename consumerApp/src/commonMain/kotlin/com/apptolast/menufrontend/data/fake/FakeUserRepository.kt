package com.apptolast.menufrontend.data.fake

import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Restaurant

class FakeUserRepository : UserRepository {

    private var userAllergens = mutableSetOf(Allergen.FISH, Allergen.DAIRY)

    override suspend fun getUserAllergens(): Result<Set<Allergen>> {
        return Result.success(userAllergens.toSet())
    }

    override suspend fun updateUserAllergens(allergens: Set<Allergen>): Result<Unit> {
        userAllergens = allergens.toMutableSet()
        return Result.success(Unit)
    }

    override suspend fun getFavoriteRestaurants(): Result<List<Restaurant>> {
        return Result.success(emptyList())
    }

    override suspend fun toggleFavorite(restaurantId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
