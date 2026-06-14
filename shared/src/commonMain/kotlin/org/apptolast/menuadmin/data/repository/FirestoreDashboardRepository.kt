package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.DashboardStats
import org.apptolast.menuadmin.domain.repository.DashboardRepository
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.domain.repository.RestaurantRepository

/**
 * Firestore-only [DashboardRepository]: computes stats client-side from the Firestore-backed repos
 * (no backend, no dishes). Allergen frequency comes from recipes' computed allergens.
 */
class FirestoreDashboardRepository(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val menuRepository: MenuRepository,
    private val restaurantRepository: RestaurantRepository,
) : DashboardRepository {
    override fun getDashboardStats(): Flow<DashboardStats> =
        combine(
            ingredientRepository.getAllIngredients(),
            recipeRepository.getAllRecipes(),
            menuRepository.getAllMenus(),
            restaurantRepository.getAllRestaurants(),
        ) { ingredients, recipes, menus, restaurants ->
            val allergenFrequency = mutableMapOf<AllergenType, Int>()
            for (recipe in recipes) {
                for (allergen in recipe.computedAllergens) {
                    allergenFrequency[allergen] = (allergenFrequency[allergen] ?: 0) + 1
                }
            }
            DashboardStats(
                totalIngredients = ingredients.size,
                activeRecipes = recipes.count { it.isActive },
                totalMenus = menus.size,
                totalRestaurants = restaurants.size,
                recentActivity = emptyList(),
                allergenFrequency = allergenFrequency.toMap(),
            )
        }
}
