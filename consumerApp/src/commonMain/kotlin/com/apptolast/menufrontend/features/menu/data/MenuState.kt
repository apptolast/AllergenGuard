package com.apptolast.menufrontend.features.menu.data

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish

/** How the safe dishes are ordered/grouped in the menu. */
enum class DishSortMode { NAME, PRICE, CATEGORY }

data class MenuState(
    val restaurantName: String = "",
    val restaurantDescription: String = "",
    val allDishes: List<Dish> = emptyList(),
    val filteredDishes: List<Dish> = emptyList(),
    val activeFilters: Set<Allergen> = emptySet(),
    val userAllergens: Set<Allergen> = emptySet(),
    val sortMode: DishSortMode = DishSortMode.NAME,
    val sortAscending: Boolean = true,
    val isFavorite: Boolean = false,
    /** When true, dishes the user can't eat are also shown (highlighted), instead of hidden. */
    val showUnsafe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val safeDishCount: Int
        get() = filteredDishes.count { dish ->
            dish.allergens.none { it in userAllergens }
        }
}

sealed interface MenuAction {
    data class ToggleAllergenFilter(val allergen: Allergen) : MenuAction

    /** Re-applies the user's saved profile allergens as the active filters. */
    data object RestoreUserFilters : MenuAction
    data class SetSortMode(val mode: DishSortMode) : MenuAction
    data object ToggleSortDirection : MenuAction
    data object ToggleFavorite : MenuAction
    data object ToggleShowUnsafe : MenuAction
    data class DishClicked(val dishId: String) : MenuAction
    data object NavigateBack : MenuAction
}
