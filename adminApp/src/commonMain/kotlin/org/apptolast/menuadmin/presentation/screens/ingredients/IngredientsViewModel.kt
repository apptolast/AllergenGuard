package org.apptolast.menuadmin.presentation.screens.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.repository.IngredientRepository

class IngredientsViewModel(
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {
    private val _formState = MutableStateFlow(IngredientsUiState())

    val uiState: StateFlow<IngredientsUiState> = combine(
        ingredientRepository.getAllIngredients(),
        _formState,
    ) { ingredients, formState ->
        var filtered = ingredients
        if (formState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { ingredient ->
                ingredient.name.contains(formState.searchQuery, ignoreCase = true) ||
                    ingredient.brand.contains(formState.searchQuery, ignoreCase = true)
            }
        }
        if (formState.filterAllergens.isNotEmpty()) {
            filtered = filtered.filter { ingredient ->
                ingredient.allergenTypes.any { it in formState.filterAllergens }
            }
        }
        formState.copy(
            isLoading = false,
            ingredients = filtered.sortedBy { it.name.lowercase() },
        )
    }
        .catch { throwable ->
            emit(
                _formState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Error al cargar ingredientes",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IngredientsUiState(),
        )

    fun onSearchQueryChange(query: String) {
        _formState.value = _formState.value.copy(searchQuery = query)
    }

    fun onToggleAllergenFilter(allergen: AllergenType) {
        val current = _formState.value.filterAllergens
        val updated = if (allergen in current) current - allergen else current + allergen
        _formState.value = _formState.value.copy(filterAllergens = updated)
    }

    fun onClearAllergenFilters() {
        _formState.value = _formState.value.copy(filterAllergens = emptySet())
    }

    fun onNewIngredient() {
        _formState.value = _formState.value.copy(
            isEditing = true,
            editingIngredient = null,
            formName = "",
            formDescription = "",
            formBrand = "",
            formLabelInfo = "",
            formAllergens = emptyMap(),
            error = null,
        )
    }

    fun onEditIngredient(ingredient: Ingredient) {
        _formState.value = _formState.value.copy(
            isEditing = true,
            editingIngredient = ingredient,
            formName = ingredient.name,
            formDescription = ingredient.description,
            formBrand = ingredient.brand,
            formLabelInfo = ingredient.labelInfo,
            formAllergens = ingredient.allergens.mapNotNull { allergen ->
                AllergenType.fromApiCode(allergen.allergenCode)?.let { type ->
                    type to allergen.containmentLevel
                }
            }.toMap(),
            error = null,
        )
    }

    fun onDismissEditor() {
        _formState.value = _formState.value.copy(
            isEditing = false,
            isSaving = false,
            editingIngredient = null,
            formName = "",
            formDescription = "",
            formBrand = "",
            formLabelInfo = "",
            formAllergens = emptyMap(),
            error = null,
        )
    }

    fun onFormNameChange(value: String) {
        _formState.value = _formState.value.copy(formName = value)
    }

    fun onFormDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(formDescription = value)
    }

    fun onFormBrandChange(value: String) {
        _formState.value = _formState.value.copy(formBrand = value)
    }

    fun onFormLabelInfoChange(value: String) {
        _formState.value = _formState.value.copy(formLabelInfo = value)
    }

    fun onToggleAllergen(allergen: AllergenType) {
        val current = _formState.value.formAllergens
        val currentLevel = current[allergen]
        val updated = when (currentLevel) {
            null -> current + (allergen to ContainmentLevel.CONTAINS)
            ContainmentLevel.CONTAINS -> current + (allergen to ContainmentLevel.MAY_CONTAIN)
            else -> current - allergen
        }
        _formState.value = _formState.value.copy(formAllergens = updated)
    }

    fun onAllergenLevelChange(
        allergen: AllergenType,
        level: ContainmentLevel,
    ) {
        val current = _formState.value.formAllergens
        if (allergen in current) {
            _formState.value = _formState.value.copy(formAllergens = current + (allergen to level))
        }
    }

    fun onSaveIngredient() {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isSaving = true, error = null)
            try {
                val state = _formState.value
                val existing = state.editingIngredient
                val allergenList = state.formAllergens.map { (type, level) ->
                    IngredientAllergen(
                        allergenCode = type.apiCode,
                        allergenName = type.nameEs,
                        containmentLevel = level,
                    )
                }

                if (existing != null) {
                    ingredientRepository.updateIngredient(
                        existing.copy(
                            name = state.formName,
                            description = state.formDescription,
                            brand = state.formBrand,
                            labelInfo = state.formLabelInfo,
                            allergens = allergenList,
                        ),
                    )
                } else {
                    ingredientRepository.addIngredient(
                        Ingredient(
                            name = state.formName,
                            description = state.formDescription,
                            brand = state.formBrand,
                            labelInfo = state.formLabelInfo,
                            allergens = allergenList,
                        ),
                    )
                }
                onDismissEditor()
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Error al guardar ingrediente",
                )
            }
        }
    }

    fun onDeleteIngredient(id: String) {
        viewModelScope.launch {
            try {
                ingredientRepository.deleteIngredient(id)
                onDismissEditor()
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    error = e.message ?: "Error al eliminar ingrediente",
                )
            }
        }
    }

    fun onDismissError() {
        _formState.value = _formState.value.copy(error = null)
    }
}
