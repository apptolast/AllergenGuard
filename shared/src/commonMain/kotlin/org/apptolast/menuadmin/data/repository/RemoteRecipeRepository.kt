package org.apptolast.menuadmin.data.repository

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.apptolast.menuadmin.data.remote.mapper.toDomain
import org.apptolast.menuadmin.data.remote.recipe.CreateRecipeRequestDto
import org.apptolast.menuadmin.data.remote.recipe.RecipeIngredientRequestDto
import org.apptolast.menuadmin.data.remote.recipe.RecipeService
import org.apptolast.menuadmin.data.remote.recipe.UpdateRecipeRequestDto
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.repository.RecipeRepository

class RemoteRecipeRepository(
    private val recipeService: RecipeService,
) : RecipeRepository {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private var loadedRestaurantId: String? = null

    override fun getAllRecipes(): Flow<List<Recipe>> = _recipes

    override fun getRecipesByRestaurant(restaurantId: String): Flow<List<Recipe>> =
        flow {
            if (loadedRestaurantId != restaurantId) {
                try {
                    refreshRecipes(restaurantId)
                } catch (e: ClientRequestException) {
                    if (e.response.status != HttpStatusCode.BadRequest) throw e
                } catch (_: Exception) {
                }
            }
            emitAll(_recipes)
        }

    private suspend fun refreshRecipes(restaurantId: String) {
        val summaries = recipeService.getRecipes(restaurantId)
        // Load full details in parallel to get ingredients and computedAllergens
        _recipes.value = coroutineScope {
            summaries.map { summary ->
                async {
                    try {
                        recipeService.getRecipeById(summary.id).toDomain()
                    } catch (_: Exception) {
                        summary.toDomain()
                    }
                }
            }.awaitAll()
        }
        loadedRestaurantId = restaurantId
    }

    override suspend fun getRecipeById(id: String): Recipe? {
        return try {
            recipeService.getRecipeById(id).toDomain()
        } catch (_: Exception) {
            _recipes.value.find { it.id == id }
        }
    }

    override suspend fun addRecipe(recipe: Recipe): Recipe {
        val response = recipeService.createRecipe(
            restaurantId = recipe.restaurantId,
            request = CreateRecipeRequestDto(
                restaurantId = recipe.restaurantId,
                name = recipe.name,
                description = recipe.description.ifEmpty { null },
                category = recipe.category.ifEmpty { null },
                price = if (recipe.price > 0) recipe.price else null,
                ingredients = recipe.ingredients.map { it.toRequest() },
            ),
        )
        val created = response.toDomain()
        loadedRestaurantId?.let { refreshRecipes(it) }
        return created
    }

    override suspend fun updateRecipe(recipe: Recipe): Recipe {
        val response = recipeService.updateRecipe(
            id = recipe.id,
            request = UpdateRecipeRequestDto(
                name = recipe.name,
                description = recipe.description.ifEmpty { null },
                category = recipe.category.ifEmpty { null },
                price = if (recipe.price > 0) recipe.price else null,
                active = recipe.isActive,
                ingredients = recipe.ingredients.map { it.toRequest() },
            ),
        )
        val updated = response.toDomain()
        loadedRestaurantId?.let { refreshRecipes(it) }
        return updated
    }

    override suspend fun deleteRecipe(id: String) {
        recipeService.deleteRecipe(id)
        loadedRestaurantId?.let { refreshRecipes(it) }
    }

    override suspend fun toggleRecipeActive(id: String): Recipe {
        val current = _recipes.value.find { it.id == id }
            ?: throw NoSuchElementException("Recipe with id $id not found")
        val response = recipeService.updateRecipe(
            id = id,
            request = UpdateRecipeRequestDto(active = !current.isActive),
        )
        val updated = response.toDomain()
        loadedRestaurantId?.let { refreshRecipes(it) }
        return updated
    }

    override suspend fun replaceAll(recipes: List<Recipe>) {
        // Not applicable for remote — API is the source of truth
        loadedRestaurantId?.let { refreshRecipes(it) }
    }

    private fun org.apptolast.menuadmin.domain.model.RecipeIngredient.toRequest() =
        RecipeIngredientRequestDto(
            ingredientId = ingredientId,
            quantity = if (quantity > 0) quantity else null,
            unit = unit.ifEmpty { null },
        )
}
