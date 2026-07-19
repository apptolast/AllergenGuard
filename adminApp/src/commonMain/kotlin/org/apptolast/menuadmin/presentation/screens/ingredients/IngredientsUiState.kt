package org.apptolast.menuadmin.presentation.screens.ingredients

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Ingredient

data class IngredientsUiState(
    val isLoading: Boolean = true,
    // Whether the current user may write the ingredient catalog (ACCOUNT_ADMIN). Managers see it read-only.
    val isAccountAdmin: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val searchQuery: String = "",
    val filterAllergens: Set<AllergenType> = emptySet(),
    val filterBrands: Set<String> = emptySet(),
    val availableBrands: List<String> = emptyList(),
    val isEditing: Boolean = false,
    val editingIngredient: Ingredient? = null,
    val isSaving: Boolean = false,
    // Form fields
    val formName: String = "",
    val formDescription: String = "",
    val formBrand: String = "",
    val formLabelInfo: String = "",
    val formAllergens: Map<AllergenType, ContainmentLevel> = emptyMap(),
    val error: String? = null,
)
