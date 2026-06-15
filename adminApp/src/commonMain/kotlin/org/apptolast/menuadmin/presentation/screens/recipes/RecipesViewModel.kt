package org.apptolast.menuadmin.presentation.screens.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository

class RecipesViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val restaurantId: String,
) : ViewModel() {
    private val _formState = MutableStateFlow(RecipesUiState())

    val uiState: StateFlow<RecipesUiState> = combine(
        recipeRepository.getRecipesByRestaurant(restaurantId),
        ingredientRepository.getAllIngredients(),
        _formState,
    ) { recipes, ingredients, formState ->
        val filtered = if (formState.searchQuery.isBlank()) {
            recipes
        } else {
            recipes.filter { recipe ->
                recipe.name.contains(formState.searchQuery, ignoreCase = true) ||
                    recipe.description.contains(formState.searchQuery, ignoreCase = true)
            }
        }
        val categories = recipes
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        formState.copy(
            isLoading = false,
            recipes = filtered,
            allIngredients = ingredients,
            availableCategories = categories,
        )
    }
        .catch { throwable ->
            emit(
                _formState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Error al cargar recetas",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecipesUiState(),
        )

    fun onSearchQueryChange(query: String) {
        _formState.value = _formState.value.copy(searchQuery = query)
    }

    fun onNewRecipe() {
        _formState.value = _formState.value.copy(
            isEditing = true,
            editingRecipe = null,
            formName = "",
            formDescription = "",
            formCategory = "",
            formPrice = "",
            formIngredients = emptyList(),
            formIsActive = true,
        )
    }

    fun onEditRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val fullRecipe = recipeRepository.getRecipeById(recipe.id) ?: recipe
            _formState.value = _formState.value.copy(
                isEditing = true,
                editingRecipe = fullRecipe,
                formName = fullRecipe.name,
                formDescription = fullRecipe.description,
                formCategory = fullRecipe.category,
                formPrice = if (fullRecipe.price > 0) fullRecipe.price.toString() else "",
                formIngredients = fullRecipe.ingredients,
                formIsActive = fullRecipe.isActive,
            )
        }
    }

    fun onDismissEditor() {
        _formState.value = _formState.value.copy(
            isEditing = false,
            isSaving = false,
            editingRecipe = null,
            formName = "",
            formDescription = "",
            formCategory = "",
            formPrice = "",
            formIngredients = emptyList(),
            formIsActive = true,
        )
    }

    fun onFormPriceChange(value: String) {
        _formState.value = _formState.value.copy(formPrice = value)
    }

    fun onFormNameChange(value: String) {
        _formState.value = _formState.value.copy(formName = value)
    }

    fun onFormDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(formDescription = value)
    }

    fun onFormCategoryChange(category: String) {
        _formState.value = _formState.value.copy(formCategory = category)
    }

    fun onToggleActive() {
        _formState.value = _formState.value.copy(formIsActive = !_formState.value.formIsActive)
    }

    fun onAddIngredientToForm(ingredient: RecipeIngredient) {
        val current = _formState.value.formIngredients
        _formState.value = _formState.value.copy(formIngredients = current + ingredient)
    }

    fun onRemoveIngredientFromForm(ingredientId: String) {
        val current = _formState.value.formIngredients
        _formState.value = _formState.value.copy(
            formIngredients = current.filter { it.ingredientId != ingredientId },
        )
    }

    fun onSaveRecipe() {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isSaving = true, error = null)
            try {
                val state = _formState.value
                val existing = state.editingRecipe
                val parsedPrice = state.formPrice.toDoubleOrNull() ?: 0.0

                if (existing != null) {
                    recipeRepository.updateRecipe(
                        existing.copy(
                            name = state.formName,
                            description = state.formDescription,
                            category = state.formCategory,
                            price = parsedPrice,
                            ingredients = state.formIngredients,
                            isActive = state.formIsActive,
                        ),
                    )
                } else {
                    recipeRepository.addRecipe(
                        Recipe(
                            restaurantId = restaurantId,
                            name = state.formName,
                            description = state.formDescription,
                            category = state.formCategory,
                            price = parsedPrice,
                            ingredients = state.formIngredients,
                            isActive = state.formIsActive,
                        ),
                    )
                }
                onDismissEditor()
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Error al guardar receta",
                )
            }
        }
    }

    fun onDeleteRecipe(id: String) {
        viewModelScope.launch {
            try {
                recipeRepository.deleteRecipe(id)
                onDismissEditor()
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    error = e.message ?: "Error al eliminar receta",
                )
            }
        }
    }
}
