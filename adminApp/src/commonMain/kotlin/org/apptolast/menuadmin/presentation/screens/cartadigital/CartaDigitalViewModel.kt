package org.apptolast.menuadmin.presentation.screens.cartadigital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository

class CartaDigitalViewModel(
    menuRepository: MenuRepository,
    recipeRepository: RecipeRepository,
    private val restaurantId: String,
) : ViewModel() {
    private val _selectedAllergens = MutableStateFlow<Set<AllergenType>>(emptySet())
    private val _selectedMenuId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CartaDigitalUiState> = combine(
        menuRepository.getMenusByRestaurant(restaurantId),
        recipeRepository.getRecipesByRestaurant(restaurantId),
        _selectedAllergens,
        _selectedMenuId,
    ) { menus, allRecipes, allergens, menuId ->
        val effectiveMenuId = menuId ?: menus.firstOrNull()?.id
        val selectedMenu = menus.find { it.id == effectiveMenuId }

        // Only show recipes that are associated with this menu
        val menuRecipeIds = selectedMenu?.recipes?.map { it.id }?.toSet().orEmpty()
        val recipes = allRecipes.filter { it.id in menuRecipeIds }

        val (safe, unsafe) = if (allergens.isEmpty()) {
            recipes to emptyList()
        } else {
            recipes.partition { recipe ->
                recipe.computedAllergens.none { it in allergens }
            }
        }

        CartaDigitalUiState(
            isLoading = false,
            availableMenus = menus,
            selectedMenuId = effectiveMenuId,
            selectedAllergens = allergens,
            allRecipes = recipes,
            safeRecipes = safe,
            unsafeRecipes = unsafe,
        )
    }
        .catch { throwable ->
            emit(
                CartaDigitalUiState(
                    isLoading = false,
                    error = throwable.message ?: "Error al cargar carta digital",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CartaDigitalUiState(),
        )

    fun onToggleAllergen(allergen: AllergenType) {
        val current = _selectedAllergens.value
        _selectedAllergens.value = if (allergen in current) current - allergen else current + allergen
    }

    fun onSelectMenu(menuId: String) {
        _selectedMenuId.value = menuId
    }

    fun onClearAllergens() {
        _selectedAllergens.value = emptySet()
    }
}
