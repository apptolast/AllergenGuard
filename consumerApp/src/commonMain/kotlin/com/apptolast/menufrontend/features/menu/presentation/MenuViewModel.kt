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

    private var restaurantId: String? = null

    fun loadMenu(restaurantId: String) {
        this.restaurantId = restaurantId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Load restaurant name + description (shown in the top bar)
            restaurantRepository.getRestaurantName(restaurantId)
                .onSuccess { name -> _state.update { it.copy(restaurantName = name) } }
            restaurantRepository.getRestaurantDescription(restaurantId)
                .onSuccess { desc -> _state.update { it.copy(restaurantDescription = desc) } }

            // Load user allergens
            userRepository.getUserAllergens()
                .onSuccess { allergens ->
                    _state.update { it.copy(userAllergens = allergens, activeFilters = allergens) }
                }

            // Is this restaurant a favorite?
            userRepository.getFavoriteRestaurants()
                .onSuccess { favorites ->
                    _state.update { it.copy(isFavorite = favorites.any { r -> r.id == restaurantId }) }
                }

            // Load menu
            restaurantRepository.getRestaurantMenu(restaurantId)
                .onSuccess { dishes ->
                    _state.update {
                        it.copy(
                            allDishes = dishes,
                            filteredDishes = displayedDishes(dishes, it.activeFilters, it.showUnsafe),
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
                        filteredDishes = displayedDishes(it.allDishes, updated, it.showUnsafe),
                    )
                }
            }
            MenuAction.RestoreUserFilters -> {
                _state.update {
                    it.copy(
                        activeFilters = it.userAllergens,
                        filteredDishes = displayedDishes(it.allDishes, it.userAllergens, it.showUnsafe),
                    )
                }
            }

            is MenuAction.SetSortMode -> _state.update { it.copy(sortMode = action.mode) }
            MenuAction.ToggleSortDirection -> _state.update { it.copy(sortAscending = !it.sortAscending) }
            MenuAction.ToggleShowUnsafe -> _state.update {
                val show = !it.showUnsafe
                it.copy(
                    showUnsafe = show,
                    filteredDishes = displayedDishes(it.allDishes, it.activeFilters, show),
                )
            }

            MenuAction.ToggleFavorite -> toggleFavorite()
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

    /** Dishes to display: all of them when [showUnsafe], otherwise only the ones safe for the filters. */
    private fun displayedDishes(
        dishes: List<Dish>,
        filters: Set<Allergen>,
        showUnsafe: Boolean,
    ): List<Dish> = if (showUnsafe) dishes else filterDishes(dishes, filters)

    private fun toggleFavorite() {
        val rid = restaurantId ?: return
        val newValue = !_state.value.isFavorite
        _state.update { it.copy(isFavorite = newValue) } // optimistic
        viewModelScope.launch {
            userRepository.toggleFavorite(rid).onFailure {
                _state.update { it.copy(isFavorite = !newValue) } // revert
            }
        }
    }
}
