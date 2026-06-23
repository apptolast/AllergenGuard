package org.apptolast.menuadmin.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.domain.repository.AuthRepository
import org.apptolast.menuadmin.domain.repository.MembershipRepository
import org.apptolast.menuadmin.domain.repository.PlatformAdminRepository

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val membershipRepository: MembershipRepository,
    private val platformAdminRepository: PlatformAdminRepository,
    private val currentAccountHolder: CurrentAccountHolder,
) : ViewModel() {
    // Don't enter the app until the tenant membership is resolved. If a token is already stored, start
    // in the "resolving" state and resolve before flipping isAuthenticated (so scoped repos always have
    // an account).
    private val _uiState = MutableStateFlow(AuthUiState(isResolvingSession = authRepository.isLoggedIn))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        if (authRepository.isLoggedIn) resolveSessionAndEnter()
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onToggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
    }

    fun onLogin() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Introduce email y contraseña") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.login(state.email.trim().lowercase(), state.password)
                resolveSessionAndEnter()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al iniciar sesión: ${e.message ?: "Error desconocido"}",
                    )
                }
            }
        }
    }

    fun onRegister() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Introduce email y contraseña") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 8 caracteres") }
            return
        }
        val email = state.email.trim().lowercase()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Invitation gate: only emails the platform owner has invited can register. The invitation
                // also carries the account + role; the membership is materialized in resolveSessionAndEnter().
                if (membershipRepository.getInvitation(email) == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Este correo no está autorizado para registrarse. " +
                                "Contacta con el administrador para que te dé de alta.",
                        )
                    }
                    return@launch
                }
                authRepository.registerAdmin(
                    email,
                    state.password,
                    state.name.trim().ifEmpty { null },
                )
                resolveSessionAndEnter()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al registrarse: ${e.message ?: "Error desconocido"}",
                    )
                }
            }
        }
    }

    fun onLogout() {
        authRepository.logout()
        currentAccountHolder.clear()
        _uiState.update { AuthUiState(isAuthenticated = false) }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Resolves the signed-in user's [org.apptolast.menuadmin.domain.model.AccountSession] (membership, or
     * materialized from an invitation on first login) into [currentAccountHolder], then enters the app.
     * A user with neither membership nor invitation is signed out (no account-less access).
     */
    private fun resolveSessionAndEnter() {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUserId
                val email = authRepository.currentUserEmail
                if (uid == null) {
                    failNoAccess()
                    return@launch
                }
                val isSuperAdmin = platformAdminRepository.isSuperAdmin(uid)
                var session = membershipRepository.getMembership(uid)
                if (session == null && email != null) {
                    membershipRepository.getInvitation(email)?.let { invitation ->
                        session = membershipRepository.materializeMembership(uid, email, invitation)
                    }
                }
                val resolved = session
                // Enter if the user is a platform owner OR has an account (membership/invitation).
                if (resolved == null && !isSuperAdmin) {
                    failNoAccess()
                } else {
                    resolved?.let { currentAccountHolder.set(it) }
                    currentAccountHolder.setSuperAdmin(isSuperAdmin)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isResolvingSession = false,
                            isAuthenticated = true,
                            error = null,
                        )
                    }
                }
            } catch (e: Exception) {
                // Transient error (e.g. network): keep the token and let the user retry; don't lock them out.
                currentAccountHolder.clear()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isResolvingSession = false,
                        isAuthenticated = false,
                        error = "No se pudo cargar tu cuenta: ${e.message ?: "Error desconocido"}",
                    )
                }
            }
        }
    }

    private fun failNoAccess() {
        authRepository.logout()
        currentAccountHolder.clear()
        _uiState.update {
            it.copy(
                isLoading = false,
                isResolvingSession = false,
                isAuthenticated = false,
                error = "Tu cuenta no tiene acceso. Pide al administrador que te invite.",
            )
        }
    }
}
