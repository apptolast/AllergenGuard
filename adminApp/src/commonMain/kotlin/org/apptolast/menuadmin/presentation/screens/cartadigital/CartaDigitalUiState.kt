package org.apptolast.menuadmin.presentation.screens.cartadigital

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe

data class CartaDigitalUiState(
    val isLoading: Boolean = true,
    val availableMenus: List<Menu> = emptyList(),
    val selectedMenuId: String? = null,
    val selectedAllergens: Set<AllergenType> = emptySet(),
    val allRecipes: List<Recipe> = emptyList(),
    val safeRecipes: List<Recipe> = emptyList(),
    val unsafeRecipes: List<Recipe> = emptyList(),
    val error: String? = null,
)
