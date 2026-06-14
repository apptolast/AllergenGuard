package org.apptolast.menuadmin.data.repository

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.apptolast.menuadmin.data.SelectedRestaurantHolder
import org.apptolast.menuadmin.data.remote.dish.DishAllergenRequestDto
import org.apptolast.menuadmin.data.remote.dish.DishRequestDto
import org.apptolast.menuadmin.data.remote.dish.DishService
import org.apptolast.menuadmin.data.remote.mapper.toDomain
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Dish
import org.apptolast.menuadmin.domain.repository.DishRepository

class RemoteDishRepository(
    private val dishService: DishService,
    private val selectedRestaurantHolder: SelectedRestaurantHolder,
) : DishRepository {
    private val _dishes = MutableStateFlow<List<Dish>>(emptyList())
    private var loadedRestaurantId: String? = null

    override fun getAllDishes(): Flow<List<Dish>> =
        flow {
            val restaurantId = selectedRestaurantHolder.selected.value?.id
            if (restaurantId != null && loadedRestaurantId != restaurantId) {
                try {
                    refreshDishes(restaurantId)
                } catch (e: ClientRequestException) {
                    if (e.response.status != HttpStatusCode.BadRequest) throw e
                } catch (_: Exception) {
                }
            }
            emitAll(_dishes)
        }

    private suspend fun refreshDishes(restaurantId: String) {
        _dishes.value = dishService.getDishes(restaurantId).map { it.toDomain() }
        loadedRestaurantId = restaurantId
    }

    override suspend fun createDish(
        name: String,
        sectionId: String,
        description: String,
        imageUrl: String?,
        allergens: List<Pair<String, ContainmentLevel>>,
    ): Dish {
        val response = dishService.createDish(
            DishRequestDto(
                name = name,
                sectionId = sectionId,
                description = description.ifEmpty { null },
                imageUrl = imageUrl,
                allergens = allergens.map { (code, level) ->
                    DishAllergenRequestDto(
                        allergenCode = code,
                        containmentLevel = level.apiValue,
                    )
                }.ifEmpty { null },
            ),
        )
        loadedRestaurantId?.let { refreshDishes(it) }
        return response.toDomain()
    }

    override suspend fun updateDish(
        id: String,
        name: String,
        sectionId: String,
        description: String,
        imageUrl: String?,
        allergens: List<Pair<String, ContainmentLevel>>,
    ): Dish {
        val response = dishService.updateDish(
            id,
            DishRequestDto(
                name = name,
                sectionId = sectionId,
                description = description.ifEmpty { null },
                imageUrl = imageUrl,
                allergens = allergens.map { (code, level) ->
                    DishAllergenRequestDto(
                        allergenCode = code,
                        containmentLevel = level.apiValue,
                    )
                }.ifEmpty { null },
            ),
        )
        loadedRestaurantId?.let { refreshDishes(it) }
        return response.toDomain()
    }

    override suspend fun deleteDish(id: String) {
        dishService.deleteDish(id)
        loadedRestaurantId?.let { refreshDishes(it) }
    }

    override suspend fun addAllergen(
        dishId: String,
        allergenCode: String,
        level: ContainmentLevel,
        notes: String,
    ): Dish {
        val response = dishService.addAllergen(
            dishId,
            DishAllergenRequestDto(
                allergenCode = allergenCode,
                containmentLevel = level.apiValue,
                notes = notes.ifEmpty { null },
            ),
        )
        loadedRestaurantId?.let { refreshDishes(it) }
        return response.toDomain()
    }

    override suspend fun removeAllergen(
        dishId: String,
        allergenId: Int,
    ) {
        dishService.removeAllergen(dishId, allergenId)
        loadedRestaurantId?.let { refreshDishes(it) }
    }
}
