package com.apptolast.menufrontend.features.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.menufrontend.data.repository.RestaurantRepository
import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.features.menu.data.MenuAction
import com.apptolast.menufrontend.features.menu.data.MenuState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MenuEffect {
    data class NavigateToDishDetail(val dishId: String) : MenuEffect
    data object NavigateBack : MenuEffect
}

class MenuViewModel(
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MenuState())
    val state: StateFlow<MenuState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MenuEffect>()
    val effect: SharedFlow<MenuEffect> = _effect.asSharedFlow()

    fun loadMenu(restaurantId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Load restaurant name
            restaurantRepository.getRestaurantName(restaurantId)
                .onSuccess { name -> _state.update { it.copy(restaurantName = name) } }

            // Load user allergens
            userRepository.getUserAllergens()
                .onSuccess { allergens ->
                    _state.update { it.copy(userAllergens = allergens, activeFilters = allergens) }
                }

            // Load menu
            restaurantRepository.getRestaurantMenu(restaurantId)
                .onSuccess { dishes ->
                    _state.update {
                        it.copy(
                            allDishes = dishes,
                            filteredDishes = filterDishes(dishes, it.activeFilters),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun onAction(action: MenuAction) {
        when (action) {
            is MenuAction.ToggleAllergenFilter -> {
                val current = _state.value.activeFilters
                val updated = if (action.allergen in current) {
                    current - action.allergen
                } else {
                    current + action.allergen
                }
                _state.update {
                    it.copy(
                        activeFilters = updated,
                        filteredDishes = filterDishes(it.allDishes, updated),
                    )
                }
            }
            is MenuAction.DishClicked -> {
                viewModelScope.launch {
                    _effect.emit(MenuEffect.NavigateToDishDetail(action.dishId))
                }
            }
            MenuAction.NavigateBack -> {
                viewModelScope.launch { _effect.emit(MenuEffect.NavigateBack) }
            }
        }
    }

    private fun filterDishes(dishes: List<Dish>, filters: Set<Allergen>): List<Dish> {
        if (filters.isEmpty()) return dishes
        return dishes.filter { dish ->
            dish.allergens.none { it in filters }
        }
    }
}
