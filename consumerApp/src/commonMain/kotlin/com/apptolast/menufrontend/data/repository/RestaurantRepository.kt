package com.apptolast.menufrontend.data.repository

import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.domain.model.Restaurant

interface RestaurantRepository {
    suspend fun getNearbyRestaurants(): Result<List<Restaurant>>
    suspend fun searchRestaurants(query: String): Result<List<Restaurant>>
    suspend fun getRestaurantMenu(restaurantId: String): Result<List<Dish>>
    suspend fun getDishDetail(dishId: String): Result<Dish>
    suspend fun getRestaurantName(restaurantId: String): Result<String>
}
