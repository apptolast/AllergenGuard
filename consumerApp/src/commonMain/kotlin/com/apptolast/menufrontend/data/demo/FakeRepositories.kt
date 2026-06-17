package com.apptolast.menufrontend.data.demo

import com.apptolast.menufrontend.data.repository.AuthRepository
import com.apptolast.menufrontend.data.repository.RestaurantRepository
import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.domain.model.Restaurant
import com.apptolast.menufrontend.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory repositories returning [ScreenshotDemoData], bound by Koin only in
 * [com.apptolast.menufrontend.core.screenshot.ScreenshotMode] so screenshots render without auth or
 * network. Never used in production builds.
 */
class FakeAuthRepository : AuthRepository {
    override val currentUser: StateFlow<User?> = MutableStateFlow(ScreenshotDemoData.user).asStateFlow()
    override val isGoogleSignInAvailable: Boolean = false
    override val isAppleSignInAvailable: Boolean = false

    override suspend fun login(email: String, password: String): Result<User> =
        Result.success(ScreenshotDemoData.user)

    override suspend fun register(email: String, password: String, name: String): Result<User> =
        Result.success(ScreenshotDemoData.user)

    override suspend fun loginWithGoogle(): Result<User> = Result.success(ScreenshotDemoData.user)
    override suspend fun loginWithApple(): Result<User> = Result.success(ScreenshotDemoData.user)
    override suspend fun logout() {}
    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}

class FakeRestaurantRepository : RestaurantRepository {
    override suspend fun getNearbyRestaurants(): Result<List<Restaurant>> =
        Result.success(ScreenshotDemoData.restaurants)

    override suspend fun searchRestaurants(query: String): Result<List<Restaurant>> =
        Result.success(
            ScreenshotDemoData.restaurants.filter { it.name.contains(query, ignoreCase = true) },
        )

    override suspend fun getRestaurantMenu(restaurantId: String): Result<List<Dish>> =
        Result.success(ScreenshotDemoData.menu)

    override suspend fun getDishDetail(dishId: String): Result<Dish> =
        Result.success(
            ScreenshotDemoData.menu.firstOrNull { it.id == dishId } ?: ScreenshotDemoData.menu.first(),
        )

    override suspend fun getRestaurantName(restaurantId: String): Result<String> =
        Result.success(
            ScreenshotDemoData.restaurants.firstOrNull { it.id == restaurantId }?.name
                ?: ScreenshotDemoData.restaurants.first().name,
        )

    override suspend fun getRestaurantDescription(restaurantId: String): Result<String> =
        Result.success(ScreenshotDemoData.RESTAURANT_DESCRIPTION)
}

class FakeUserRepository : UserRepository {
    override suspend fun getUserAllergens(): Result<Set<Allergen>> =
        Result.success(ScreenshotDemoData.allergens)

    override suspend fun updateUserAllergens(allergens: Set<Allergen>): Result<Unit> = Result.success(Unit)

    override suspend fun getFavoriteRestaurants(): Result<List<Restaurant>> =
        Result.success(ScreenshotDemoData.favoriteRestaurants)

    override suspend fun toggleFavorite(restaurantId: String): Result<Unit> = Result.success(Unit)
}
