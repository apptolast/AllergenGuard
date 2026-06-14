package com.apptolast.menufrontend.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.menufrontend.data.repository.RestaurantRepository
import com.apptolast.menufrontend.features.home.data.HomeAction
import com.apptolast.menufrontend.features.home.data.HomeState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HomeEffect {
    data class NavigateToMenu(val restaurantId: String) : HomeEffect
}

class HomeViewModel(
    private val restaurantRepository: RestaurantRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        loadRestaurants()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = action.query) }
                searchRestaurants(action.query)
            }
            is HomeAction.RestaurantClicked -> {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.NavigateToMenu(action.restaurantId))
                }
            }
        }
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            restaurantRepository.getNearbyRestaurants()
                .onSuccess { restaurants ->
                    _state.update { it.copy(restaurants = restaurants, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun searchRestaurants(query: String) {
        viewModelScope.launch {
            restaurantRepository.searchRestaurants(query)
                .onSuccess { restaurants ->
                    _state.update { it.copy(restaurants = restaurants) }
                }
        }
    }
}
