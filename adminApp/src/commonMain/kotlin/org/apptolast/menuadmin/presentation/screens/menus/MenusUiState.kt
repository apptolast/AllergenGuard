package org.apptolast.menuadmin.presentation.screens.menus

import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe

data class MenusUiState(
    val isLoading: Boolean = true,
    val menus: List<Menu> = emptyList(),
    val selectedMenu: Menu? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val error: String? = null,
    // Full recipes with computedAllergens for the selected menu
    val menuRecipes: List<Recipe> = emptyList(),
    val isLoadingRecipes: Boolean = false,
    // Form state
    val isFormVisible: Boolean = false,
    val formName: String = "",
    val formDescription: String = "",
    val formRestaurantLogoUrl: String = "",
    val formCompanyLogoUrl: String = "",
    val formSelectedRecipeIds: Set<String> = emptySet(),
    val availableRecipes: List<Recipe> = emptyList(),
    val isSaving: Boolean = false,
    // Edit / Delete state
    val editingMenu: Menu? = null,
    val menuToDelete: Menu? = null,
)
