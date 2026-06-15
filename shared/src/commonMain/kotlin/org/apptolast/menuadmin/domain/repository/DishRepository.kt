package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Dish

interface DishRepository {
    fun getAllDishes(): Flow<List<Dish>>

    suspend fun createDish(
        name: String,
        sectionId: String,
        description: String = "",
        imageUrl: String? = null,
        allergens: List<Pair<String, ContainmentLevel>> = emptyList(),
    ): Dish

    suspend fun updateDish(
        id: String,
        name: String,
        sectionId: String,
        description: String = "",
        imageUrl: String? = null,
        allergens: List<Pair<String, ContainmentLevel>> = emptyList(),
    ): Dish

    suspend fun deleteDish(id: String)

    suspend fun addAllergen(
        dishId: String,
        allergenCode: String,
        level: ContainmentLevel,
        notes: String = "",
    ): Dish

    suspend fun removeAllergen(
        dishId: String,
        allergenId: Int,
    )
}
