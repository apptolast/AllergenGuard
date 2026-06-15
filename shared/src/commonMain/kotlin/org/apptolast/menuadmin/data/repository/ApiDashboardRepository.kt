package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.DashboardStats
import org.apptolast.menuadmin.domain.repository.DashboardRepository
import org.apptolast.menuadmin.domain.repository.DishRepository
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RestaurantRepository

class ApiDashboardRepository(
    private val menuRepository: MenuRepository,
    private val dishRepository: DishRepository,
    private val restaurantRepository: RestaurantRepository,
) : DashboardRepository {
    override fun getDashboardStats(): Flow<DashboardStats> {
        return combine(
            menuRepository.getAllMenus(),
            dishRepository.getAllDishes(),
            restaurantRepository.getAllRestaurants(),
        ) { menus, dishes, restaurants ->
            val totalSections = menus.sumOf { it.sections.size }

            val allergenFrequency = mutableMapOf<AllergenType, Int>()
            for (dish in dishes) {
                for (allergen in dish.allergens) {
                    allergenFrequency[allergen] = (allergenFrequency[allergen] ?: 0) + 1
                }
            }

            DashboardStats(
                totalIngredients = dishes.size,
                activeRecipes = totalSections,
                totalMenus = menus.size,
                totalRestaurants = restaurants.size,
                recentActivity = emptyList(),
                allergenFrequency = allergenFrequency.toMap(),
            )
        }
    }
}
