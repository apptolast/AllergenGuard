package com.apptolast.menufrontend.features.login.data

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    // Provider availability per platform: Google on Android, Apple on iOS.
    val isGoogleAvailable: Boolean = false,
    val isAppleAvailable: Boolean = false,
)

sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object TogglePasswordVisibility : LoginAction
    data object LoginClicked : LoginAction
    data object GoogleSignInClicked : LoginAction
    data object AppleSignInClicked : LoginAction
    data object RegisterClicked : LoginAction
    data object ForgotPasswordClicked : LoginAction
}
