package org.apptolast.menuadmin.data.remote.ingredient

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.apptolast.menuadmin.data.remote.ApiConstants

class IngredientService(
    private val client: HttpClient,
) {
    suspend fun getIngredients(): List<IngredientResponseDto> = client.get(ApiConstants.ADMIN_INGREDIENTS).body()

    suspend fun getIngredientById(id: String): IngredientResponseDto =
        client.get("${ApiConstants.ADMIN_INGREDIENTS}/$id").body()

    suspend fun createIngredient(request: CreateIngredientRequestDto): IngredientResponseDto =
        client.post(ApiConstants.ADMIN_INGREDIENTS) {
            setBody(request)
        }.body()

    suspend fun updateIngredient(
        id: String,
        request: UpdateIngredientRequestDto,
    ): IngredientResponseDto =
        client.put("${ApiConstants.ADMIN_INGREDIENTS}/$id") {
            setBody(request)
        }.body()

    suspend fun deleteIngredient(id: String) {
        client.delete("${ApiConstants.ADMIN_INGREDIENTS}/$id")
    }

    suspend fun searchIngredients(name: String): List<IngredientResponseDto> =
        client.get("${ApiConstants.ADMIN_INGREDIENTS}/search") {
            parameter("name", name)
        }.body()

    suspend fun getIngredientAllergens(id: String): List<IngredientAllergenResponseDto> =
        client.get("${ApiConstants.ADMIN_INGREDIENTS}/$id/allergens").body()

    suspend fun updateIngredientAllergens(
        id: String,
        allergens: List<IngredientAllergenRequestDto>,
    ): IngredientResponseDto =
        client.put("${ApiConstants.ADMIN_INGREDIENTS}/$id/allergens") {
            setBody(allergens)
        }.body()
}
