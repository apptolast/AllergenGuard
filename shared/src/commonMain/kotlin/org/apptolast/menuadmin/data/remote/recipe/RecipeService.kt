package org.apptolast.menuadmin.data.remote.recipe

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.apptolast.menuadmin.data.remote.ApiConstants

class RecipeService(
    private val client: HttpClient,
) {
    suspend fun getRecipes(restaurantId: String): List<RecipeSummaryResponseDto> =
        client.get("${ApiConstants.ADMIN_RESTAURANTS}/$restaurantId/recipes").body()

    suspend fun getRecipeById(id: String): RecipeResponseDto = client.get("${ApiConstants.ADMIN_RECIPES}/$id").body()

    suspend fun createRecipe(
        restaurantId: String,
        request: CreateRecipeRequestDto,
    ): RecipeResponseDto =
        client.post("${ApiConstants.ADMIN_RESTAURANTS}/$restaurantId/recipes") {
            setBody(request)
        }.body()

    suspend fun updateRecipe(
        id: String,
        request: UpdateRecipeRequestDto,
    ): RecipeResponseDto =
        client.put("${ApiConstants.ADMIN_RECIPES}/$id") {
            setBody(request)
        }.body()

    suspend fun deleteRecipe(id: String) {
        client.delete("${ApiConstants.ADMIN_RECIPES}/$id")
    }

    suspend fun getRecipeAllergens(id: String): List<ComputedAllergenResponseDto> =
        client.get("${ApiConstants.ADMIN_RECIPES}/$id/allergens").body()
}
