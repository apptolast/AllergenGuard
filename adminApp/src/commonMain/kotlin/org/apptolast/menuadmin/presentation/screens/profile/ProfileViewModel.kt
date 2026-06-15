package org.apptolast.menuadmin.presentation.screens.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.apptolast.menuadmin.domain.repository.AuthRepository

class ProfileViewModel(
    authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfileUiState(
            email = authRepository.currentUserEmail,
            userId = authRepository.currentUserId,
            emailVerified = authRepository.isEmailVerified,
        ),
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun dismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
