package com.apptolast.menufrontend.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.menufrontend.data.repository.AuthRepository
import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.features.profile.data.ProfileAction
import com.apptolast.menufrontend.features.profile.data.ProfileState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProfileEffect {
    data object NavigateToLogin : ProfileEffect
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.ToggleAllergen -> toggleAllergen(action)
            ProfileAction.LogoutClicked -> logout()
            ProfileAction.NotificationsClicked -> { /* TODO */ }
            ProfileAction.LanguageClicked -> { /* TODO */ }
            ProfileAction.FavoriteRestaurantsClicked -> { /* TODO */ }
            ProfileAction.HelpClicked -> { /* TODO */ }
        }
    }

    private fun toggleAllergen(action: ProfileAction.ToggleAllergen) {
        val user = _state.value.user ?: return
        val updated = if (action.allergen in user.allergens) {
            user.allergens - action.allergen
        } else {
            user.allergens + action.allergen
        }
        _state.update { it.copy(user = user.copy(allergens = updated)) }
        viewModelScope.launch {
            userRepository.updateUserAllergens(updated)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _effect.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
