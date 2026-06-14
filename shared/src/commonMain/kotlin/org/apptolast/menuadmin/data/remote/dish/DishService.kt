package org.apptolast.menuadmin.data.remote.dish

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.apptolast.menuadmin.data.remote.ApiConstants

class DishService(
    private val client: HttpClient,
) {
    suspend fun getDishes(restaurantId: String): List<DishResponseDto> =
        client.get("${ApiConstants.ADMIN_RESTAURANTS}/$restaurantId/dishes").body()

    suspend fun createDish(request: DishRequestDto): DishResponseDto =
        client.post(ApiConstants.ADMIN_DISHES) {
            setBody(request)
        }.body()

    suspend fun updateDish(
        id: String,
        request: DishRequestDto,
    ): DishResponseDto =
        client.put("${ApiConstants.ADMIN_DISHES}/$id") {
            setBody(request)
        }.body()

    suspend fun deleteDish(id: String) {
        client.delete("${ApiConstants.ADMIN_DISHES}/$id")
    }

    suspend fun addAllergen(
        dishId: String,
        request: DishAllergenRequestDto,
    ): DishResponseDto =
        client.post("${ApiConstants.ADMIN_DISHES}/$dishId/allergens") {
            setBody(request)
        }.body()

    suspend fun removeAllergen(
        dishId: String,
        allergenId: Int,
    ) {
        client.delete("${ApiConstants.ADMIN_DISHES}/$dishId/allergens/$allergenId")
    }
}
