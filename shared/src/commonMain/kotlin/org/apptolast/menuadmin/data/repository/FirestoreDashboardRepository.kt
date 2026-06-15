package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.apptolast.menuadmin.domain.model.ActivityEntry
import org.apptolast.menuadmin.domain.model.ActivityType
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.DashboardStats
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.repository.DashboardRepository
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.domain.repository.RestaurantRepository
import kotlin.time.Instant

/**
 * Firestore-only [DashboardRepository]: PLATFORM-WIDE stats computed client-side across ALL
 * restaurants (not the selected one) — the dashboard is a top-level overview, not restaurant config.
 * Allergen frequency comes from recipes' computed allergens.
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
            restaurantRepository.getAllRestaurants(),
        ) { ingredients, restaurants ->
            // Recipes/menus are nested per restaurant, so aggregate over every restaurant instead of
            // relying on the last-loaded cache (which made the dashboard depend on a selection).
            val recipes = restaurants.flatMap { recipeRepository.getRecipesByRestaurant(it.id).first() }
            val menus = restaurants.flatMap { menuRepository.getMenusByRestaurant(it.id).first() }

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
                recentActivity = recentActivity(ingredients, recipes, menus),
                allergenFrequency = allergenFrequency.toMap(),
            )
        }

    /** Newest created/updated items across the whole platform, by Firestore document time. */
    private fun recentActivity(
        ingredients: List<Ingredient>,
        recipes: List<Recipe>,
        menus: List<Menu>,
    ): List<ActivityEntry> =
        buildList {
            ingredients.forEach { add(entry("Ingrediente: ${it.name}", it.createdAt, it.updatedAt)) }
            recipes.forEach { add(entry("Receta: ${it.name}", it.createdAt, it.updatedAt)) }
            menus.forEach { add(entry("Menu: ${it.name}", it.createdAt, it.updatedAt)) }
        }.filterNotNull()
            .sortedByDescending { it.timestamp }
            .take(8)

    private fun entry(
        description: String,
        createdAt: Instant?,
        updatedAt: Instant?,
    ): ActivityEntry? {
        val timestamp = updatedAt ?: createdAt ?: return null
        if (timestamp == Instant.DISTANT_PAST) return null // no real Firestore time available
        val type = if (createdAt != null && createdAt == updatedAt) ActivityType.CREATED else ActivityType.UPDATED
        return ActivityEntry(description = description, timestamp = timestamp, type = type)
    }
}
