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
        loadAllergens()
    }

    /** Hydrates the saved allergen selection from Firestore so the chips reflect what was stored. */
    private fun loadAllergens() {
        viewModelScope.launch {
            userRepository.getUserAllergens()
                .onSuccess { allergens -> _state.update { it.copy(allergens = allergens) } }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.EditAllergiesClicked ->
                _state.update { it.copy(isEditingAllergens = true, sheetSelection = it.allergens) }

            is ProfileAction.ToggleSheetAllergen -> toggleSheetAllergen(action)
            ProfileAction.SaveAllergies -> saveAllergens()
            ProfileAction.DismissAllergenSheet ->
                _state.update { it.copy(isEditingAllergens = false) }
            ProfileAction.LogoutClicked -> logout()
            ProfileAction.NotificationsClicked -> { /* TODO */ }
            ProfileAction.LanguageClicked -> { /* TODO */ }
            ProfileAction.FavoriteRestaurantsClicked -> { /* TODO */ }
            ProfileAction.HelpClicked -> { /* TODO */ }
        }
    }

    private fun toggleSheetAllergen(action: ProfileAction.ToggleSheetAllergen) {
        _state.update {
            val updated = if (action.allergen in it.sheetSelection) {
                it.sheetSelection - action.allergen
            } else {
                it.sheetSelection + action.allergen
            }
            it.copy(sheetSelection = updated)
        }
    }

    private fun saveAllergens() {
        val selection = _state.value.sheetSelection
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            userRepository.updateUserAllergens(selection)
                .onSuccess {
                    _state.update {
                        it.copy(allergens = selection, isEditingAllergens = false, isSaving = false)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _effect.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
