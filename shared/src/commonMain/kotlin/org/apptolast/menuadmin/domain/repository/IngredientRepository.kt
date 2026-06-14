package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.Ingredient

interface IngredientRepository {
    fun getAllIngredients(): Flow<List<Ingredient>>

    suspend fun getIngredientById(id: String): Ingredient?

    suspend fun addIngredient(ingredient: Ingredient): Ingredient

    suspend fun updateIngredient(ingredient: Ingredient): Ingredient

    suspend fun deleteIngredient(id: String)

    suspend fun searchIngredients(query: String): List<Ingredient>

    suspend fun replaceAll(ingredients: List<Ingredient>)
}
