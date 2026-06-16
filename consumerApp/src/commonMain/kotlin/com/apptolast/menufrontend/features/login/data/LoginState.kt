package com.apptolast.menufrontend.features.login.data

data class LoginState(
    val email: String = "a@a.com",
    val password: String = "abcd1234",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
)

sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object TogglePasswordVisibility : LoginAction
    data object LoginClicked : LoginAction
    data object GoogleSignInClicked : LoginAction
    data object RegisterClicked : LoginAction
    data object ForgotPasswordClicked : LoginAction
}
