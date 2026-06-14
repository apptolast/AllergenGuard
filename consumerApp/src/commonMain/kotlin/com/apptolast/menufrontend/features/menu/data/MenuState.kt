package com.apptolast.menufrontend.features.menu.data

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish

data class MenuState(
    val restaurantName: String = "",
    val allDishes: List<Dish> = emptyList(),
    val filteredDishes: List<Dish> = emptyList(),
    val activeFilters: Set<Allergen> = emptySet(),
    val userAllergens: Set<Allergen> = emptySet(),
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
    data class DishClicked(val dishId: String) : MenuAction
    data object NavigateBack : MenuAction
}
