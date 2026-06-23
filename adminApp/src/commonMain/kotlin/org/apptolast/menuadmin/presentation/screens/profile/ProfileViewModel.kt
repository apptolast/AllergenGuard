package org.apptolast.menuadmin.presentation.screens.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.repository.AuthRepository

class ProfileViewModel(
    authRepository: AuthRepository,
    currentAccountHolder: CurrentAccountHolder,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfileUiState(
            name = authRepository.currentUserName,
            email = authRepository.currentUserEmail,
            userId = authRepository.currentUserId,
            roleLabel = currentAccountHolder.session.value?.role?.let(::roleLabel),
            emailVerified = authRepository.isEmailVerified,
        ),
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun dismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

/** Maps an [AccountRole] to its Spanish label shown in the profile. */
private fun roleLabel(role: AccountRole): String =
    when (role) {
        AccountRole.ACCOUNT_ADMIN -> "Administrador"
        AccountRole.RESTAURANT_MANAGER -> "Encargado de restaurante"
    }
