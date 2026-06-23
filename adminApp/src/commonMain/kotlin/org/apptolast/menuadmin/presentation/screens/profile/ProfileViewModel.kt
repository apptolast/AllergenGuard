package org.apptolast.menuadmin.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.repository.AuthRepository

class ProfileViewModel(
    private val authRepository: AuthRepository,
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

    fun onStartEditName() {
        _uiState.update { it.copy(isEditingName = true, nameDraft = it.name.orEmpty(), error = null) }
    }

    fun onNameDraftChange(value: String) {
        _uiState.update { it.copy(nameDraft = value) }
    }

    fun onCancelEditName() {
        _uiState.update { it.copy(isEditingName = false, nameDraft = "") }
    }

    fun onSaveName() {
        val draft = _uiState.value.nameDraft.trim()
        if (draft.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingName = true, error = null) }
            try {
                authRepository.updateDisplayName(draft)
                _uiState.update {
                    it.copy(
                        isSavingName = false,
                        isEditingName = false,
                        name = draft,
                        successMessage = "Nombre actualizado",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingName = false,
                        error = "No se pudo actualizar el nombre: ${e.message ?: "Error desconocido"}",
                    )
                }
            }
        }
    }

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
