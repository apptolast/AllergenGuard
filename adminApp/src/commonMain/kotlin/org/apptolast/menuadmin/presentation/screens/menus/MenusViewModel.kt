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
import org.apptolast.menuadmin.data.repository.DishImageUploader
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.MenuRecipeSummary
import org.apptolast.menuadmin.domain.platform.MenuPdfExporter
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.domain.repository.RestaurantRepository
import org.apptolast.menuadmin.platform.buildAllergenPdfPayload
import org.apptolast.menuadmin.platform.encodeAllergenPdfPayload
import org.apptolast.menuadmin.platform.launchAllergenPdf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MenusViewModel(
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository,
    private val restaurantRepository: RestaurantRepository,
    private val menuPdfExporter: MenuPdfExporter,
    private val imageUploader: DishImageUploader,
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
            isUploadingRestaurantLogo = false,
            isUploadingCompanyLogo = false,
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
            isUploadingRestaurantLogo = false,
            isUploadingCompanyLogo = false,
            formSelectedRecipeIds = menu.recipes.map { it.id }.toSet(),
        )
    }

    fun onFormNameChange(name: String) {
        _localState.value = _localState.value.copy(formName = name)
    }

    fun onFormDescriptionChange(desc: String) {
        _localState.value = _localState.value.copy(formDescription = desc)
    }

    /** Picks + compresses a logo and uploads it to Firebase Storage; the resulting URL is CORS-safe
     * (same bucket as dish photos), so it can be embedded in the exported PDF. */
    fun onPickRestaurantLogo() {
        viewModelScope.launch {
            _localState.value = _localState.value.copy(isUploadingRestaurantLogo = true, error = null)
            try {
                val url = imageUploader.pickCompressAndUpload(restaurantId)
                _localState.value = _localState.value.copy(
                    isUploadingRestaurantLogo = false,
                    formRestaurantLogoUrl = url ?: _localState.value.formRestaurantLogoUrl,
                )
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    isUploadingRestaurantLogo = false,
                    error = e.message ?: "Error al subir el logo",
                )
            }
        }
    }

    fun onRemoveRestaurantLogo() {
        _localState.value = _localState.value.copy(formRestaurantLogoUrl = "")
    }

    fun onPickCompanyLogo() {
        viewModelScope.launch {
            _localState.value = _localState.value.copy(isUploadingCompanyLogo = true, error = null)
            try {
                val url = imageUploader.pickCompressAndUpload(restaurantId)
                _localState.value = _localState.value.copy(
                    isUploadingCompanyLogo = false,
                    formCompanyLogoUrl = url ?: _localState.value.formCompanyLogoUrl,
                )
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    isUploadingCompanyLogo = false,
                    error = e.message ?: "Error al subir el logo",
                )
            }
        }
    }

    fun onRemoveCompanyLogo() {
        _localState.value = _localState.value.copy(formCompanyLogoUrl = "")
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
            isUploadingRestaurantLogo = false,
            isUploadingCompanyLogo = false,
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

    /** Activates this menu (publishes it) or deactivates it; activating unpublishes the others. */
    fun onToggleMenuPublished(menu: Menu) {
        viewModelScope.launch {
            try {
                menuRepository.setMenuPublished(restaurantId, menu.id, !menu.published)
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    error = e.message ?: "Error al cambiar el menu activo",
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

    /**
     * Builds the allergen-menu PDF for the selected menu (every recipe it contains, alphabetically,
     * ignoring the on-screen category filter) and hands it to the platform exporter, which downloads
     * it. The restaurant name for the header is fetched lazily; logos come from the menu itself.
     */
    fun exportPdf() {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val menu = state.selectedMenu ?: return@launch
                val payload = buildAllergenPdfPayload(menu, state.menuRecipes)
                launchAllergenPdf(encodeAllergenPdfPayload(payload))
            } catch (e: Exception) {
                _localState.value = _localState.value.copy(
                    error = e.message ?: "Error al exportar PDF",
                )
            }
        }
    }

    private companion object {
        /** Allergen column headers for the PDF, matching the legal Spanish naming on the reference doc. */
        val ALLERGEN_PDF_LABELS = mapOf(
            AllergenType.GLUTEN to "GLUTEN",
            AllergenType.CRUSTACEANS to "CRUSTÁCEOS",
            AllergenType.EGGS to "HUEVOS",
            AllergenType.FISH to "PESCADO",
            AllergenType.PEANUTS to "CACAHUETES",
            AllergenType.SOY to "SOJA",
            AllergenType.DAIRY to "LÁCTEOS",
            AllergenType.TREE_NUTS to "F. CÁSCARA",
            AllergenType.CELERY to "APIO",
            AllergenType.MUSTARD to "MOSTAZA",
            AllergenType.SESAME to "SÉSAMO",
            AllergenType.SULFITES to "SULFITOS",
            AllergenType.LUPINS to "ALTRAMUCES",
            AllergenType.MOLLUSKS to "MOLUSCOS",
        )
    }
}
