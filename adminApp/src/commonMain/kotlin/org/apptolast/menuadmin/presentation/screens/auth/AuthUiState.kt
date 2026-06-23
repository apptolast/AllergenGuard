package org.apptolast.menuadmin.presentation.screens.auth

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    /** Resolving the tenant membership of an already-signed-in user (e.g. on app start with a stored token). */
    val isResolvingSession: Boolean = false,
    val isLoginMode: Boolean = true,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val error: String? = null,
)
