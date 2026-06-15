package com.apptolast.menufrontend.features.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.features.favorites.data.FavoritesAction
import com.apptolast.menufrontend.features.favorites.data.FavoritesState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface FavoritesEffect {
    data class NavigateToMenu(val restaurantId: String) : FavoritesEffect
}

class FavoritesViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<FavoritesEffect>()
    val effect: SharedFlow<FavoritesEffect> = _effect.asSharedFlow()

    init {
        loadFavorites()
    }

    fun onAction(action: FavoritesAction) {
        when (action) {
            is FavoritesAction.RestaurantClicked -> {
                viewModelScope.launch {
                    _effect.emit(FavoritesEffect.NavigateToMenu(action.restaurantId))
                }
            }
            is FavoritesAction.RemoveFavorite -> {
                viewModelScope.launch {
                    userRepository.toggleFavorite(action.restaurantId)
                    loadFavorites()
                }
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            userRepository.getFavoriteRestaurants()
                .onSuccess { restaurants ->
                    _state.update { it.copy(restaurants = restaurants, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
