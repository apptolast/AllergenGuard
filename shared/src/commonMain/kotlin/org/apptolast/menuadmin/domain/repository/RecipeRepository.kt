package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.Recipe

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>

    fun getRecipesByRestaurant(restaurantId: String): Flow<List<Recipe>>

    suspend fun getRecipeById(id: String): Recipe?

    suspend fun addRecipe(recipe: Recipe): Recipe

    suspend fun updateRecipe(recipe: Recipe): Recipe

    suspend fun deleteRecipe(id: String)

    suspend fun toggleRecipeActive(id: String): Recipe

    suspend fun replaceAll(recipes: List<Recipe>)
}
