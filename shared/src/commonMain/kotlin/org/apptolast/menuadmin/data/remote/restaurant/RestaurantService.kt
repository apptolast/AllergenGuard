package org.apptolast.menuadmin.data.remote.restaurant

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.apptolast.menuadmin.data.remote.ApiConstants
import org.apptolast.menuadmin.data.remote.PageResponseDto

class RestaurantService(
    private val client: HttpClient,
) {
    suspend fun getRestaurants(
        page: Int = 0,
        size: Int = 20,
    ): PageResponseDto<RestaurantResponseDto> =
        client.get(ApiConstants.ADMIN_RESTAURANTS) {
            parameter("page", page)
            parameter("size", size)
        }.body()

    suspend fun getRestaurantById(id: String): RestaurantResponseDto =
        client.get("${ApiConstants.ADMIN_RESTAURANTS}/$id").body()

    suspend fun createRestaurant(request: RestaurantRequestDto): RestaurantResponseDto =
        client.post(ApiConstants.ADMIN_RESTAURANTS) {
            setBody(request)
        }.body()

    suspend fun updateRestaurant(
        id: String,
        request: RestaurantRequestDto,
    ): RestaurantResponseDto =
        client.put("${ApiConstants.ADMIN_RESTAURANTS}/$id") {
            setBody(request)
        }.body()

    suspend fun deleteRestaurant(id: String) {
        client.delete("${ApiConstants.ADMIN_RESTAURANTS}/$id")
    }
}
