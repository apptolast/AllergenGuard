package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.Restaurant

interface RestaurantRepository {
    fun getAllRestaurants(): Flow<List<Restaurant>>

    suspend fun getRestaurantById(id: String): Restaurant?

    suspend fun createRestaurant(restaurant: Restaurant): Restaurant

    suspend fun updateRestaurant(restaurant: Restaurant): Restaurant

    suspend fun deleteRestaurant(id: String)
}
