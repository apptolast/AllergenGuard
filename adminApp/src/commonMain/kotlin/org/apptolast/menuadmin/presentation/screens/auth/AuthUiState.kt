package org.apptolast.menuadmin.presentation.screens.auth

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isLoginMode: Boolean = true,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val error: String? = null,
)
