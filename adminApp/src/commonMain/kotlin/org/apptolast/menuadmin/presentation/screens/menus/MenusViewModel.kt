package org.apptolast.menuadmin.presentation.screens.menus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.MenuRecipeSummary
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MenusViewModel(
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository,
    private val restaurantId: String,
) : ViewModel() {
    private val _localState = MutableStateFlow(MenusUiState())

    val uiState: StateFlow<MenusUiState> = combine(
        menuRepository.getMenusByRestaurant(restaurantId),
        recipeRepository.getRecipesByRestaurant(restaurantId),
        _localState,
    ) { menus, recipes, localState ->
        val updatedSelectedMenu = localState.selectedMenu?.let { selected ->
            menus.find { it.id == selected.id }
        }
        localState.copy(
            isLoading = false,
            menus = menus,
            availableRecipes = recipes,
            selectedMenu = updatedSelectedMenu ?: localState.selectedMenu,
        )
    }
        .catch { throwable ->
            emit(
                _localState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Error al cargar menus",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MenusUiState(),
        )

    fun selectMenu(menu: Menu) {
        _localState.value = _localState.value.copy(
            selectedMenu = menu,
            selectedCategory = null,
            menuRecipes = emptyList(),
            isLoadingRecipes = true,
        )
        viewModelScope.launch {
            try {
                val fullRecipes = menu.recipes.mapNotNull { recipeSummary ->
                    recipeRepository.getRecipeById(recipeSummary.id)
                }
                _localState.value = _localState.value.copy(
                    menuRecipes = fullRecipes,
                    isLoadingRecipes = false,
                )
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    isLoadingRecipes = false,
                    error = e.message ?: "Error al cargar recetas del menu",
                )
            }
        }
    }

    fun clearMenuSelection() {
        _localState.value = _localState.value.copy(
            selectedMenu = null,
            selectedCategory = null,
            menuRecipes = emptyList(),
        )
    }

    fun filterByCategory(category: String?) {
        _localState.value = _localState.value.copy(selectedCategory = category)
    }

    fun onNewMenu() {
        _localState.value = _localState.value.copy(
            isFormVisible = true,
            editingMenu = null,
            formName = "",
            formDescription = "",
            formRestaurantLogoUrl = "",
            formCompanyLogoUrl = "",
            formSelectedRecipeIds = emptySet(),
        )
    }

    fun onEditMenu(menu: Menu) {
        _localState.value = _localState.value.copy(
            isFormVisible = true,
            editingMenu = menu,
            selectedMenu = null,
            formName = menu.name,
            formDescription = menu.description,
            formRestaurantLogoUrl = menu.restaurantLogoUrl ?: "",
            formCompanyLogoUrl = menu.companyLogoUrl ?: "",
            formSelectedRecipeIds = menu.recipes.map { it.id }.toSet(),
        )
    }

    fun onFormNameChange(name: String) {
        _localState.value = _localState.value.copy(formName = name)
    }

    fun onFormDescriptionChange(desc: String) {
        _localState.value = _localState.value.copy(formDescription = desc)
    }

    fun onFormRestaurantLogoUrlChange(url: String) {
        _localState.value = _localState.value.copy(formRestaurantLogoUrl = url)
    }

    fun onFormCompanyLogoUrlChange(url: String) {
        _localState.value = _localState.value.copy(formCompanyLogoUrl = url)
    }

    fun onToggleRecipeSelection(recipeId: String) {
        val current = _localState.value.formSelectedRecipeIds
        val updated = if (recipeId in current) current - recipeId else current + recipeId
        _localState.value = _localState.value.copy(formSelectedRecipeIds = updated)
    }

    fun onDismissForm() {
        _localState.value = _localState.value.copy(
            isFormVisible = false,
            isSaving = false,
            editingMenu = null,
            formName = "",
            formDescription = "",
            formRestaurantLogoUrl = "",
            formCompanyLogoUrl = "",
            formSelectedRecipeIds = emptySet(),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onSaveMenu() {
        viewModelScope.launch {
            try {
                _localState.value = _localState.value.copy(isSaving = true)
                val state = _localState.value
                val editing = state.editingMenu

                // availableRecipes lives in the combined uiState, not in _localState
                val selectedRecipes = uiState.value.availableRecipes
                    .filter { it.id in state.formSelectedRecipeIds }
                    .map { MenuRecipeSummary(id = it.id, name = it.name) }

                if (editing != null) {
                    menuRepository.updateMenu(
                        editing.copy(
                            name = state.formName,
                            description = state.formDescription,
                            restaurantLogoUrl = state.formRestaurantLogoUrl.ifBlank { null },
                            companyLogoUrl = state.formCompanyLogoUrl.ifBlank { null },
                            recipes = selectedRecipes,
                        ),
                    )
                } else {
                    menuRepository.addMenu(
                        Menu(
                            id = Uuid.random().toString(),
                            restaurantId = restaurantId,
                            name = state.formName,
                            description = state.formDescription,
                            restaurantLogoUrl = state.formRestaurantLogoUrl.ifBlank { null },
                            companyLogoUrl = state.formCompanyLogoUrl.ifBlank { null },
                            recipes = selectedRecipes,
                        ),
                    )
                }
                onDismissForm()
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    isSaving = false,
                    error = e.message ?: if (_localState.value.editingMenu != null) {
                        "Error al actualizar menu"
                    } else {
                        "Error al crear menu"
                    },
                )
            }
        }
    }

    fun onRequestDeleteMenu(menu: Menu) {
        _localState.value = _localState.value.copy(menuToDelete = menu)
    }

    fun onDismissDeleteDialog() {
        _localState.value = _localState.value.copy(menuToDelete = null)
    }

    fun onConfirmDeleteMenu() {
        val menu = _localState.value.menuToDelete ?: return
        viewModelScope.launch {
            try {
                menuRepository.deleteMenu(menu.id)
                _localState.value = _localState.value.copy(
                    menuToDelete = null,
                    selectedMenu = null,
                )
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    menuToDelete = null,
                    error = e.message ?: "Error al eliminar menu",
                )
            }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            try {
                val menu = _localState.value.selectedMenu ?: return@launch
                menuRepository.exportMenuToJson(menu.id)
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    error = e.message ?: "Error al exportar JSON",
                )
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            try {
                // PDF export would be handled by platform-specific code
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    error = e.message ?: "Error al exportar PDF",
                )
            }
        }
    }
}
