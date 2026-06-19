package com.apptolast.menufrontend.features.login.data

data class LoginState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    // Same screen serves login and sign-up; this toggles which form/action is shown.
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    // Provider availability per platform: Google on Android, Apple on iOS.
    val isGoogleAvailable: Boolean = false,
    val isAppleAvailable: Boolean = false,
)

sealed interface LoginAction {
    data class NameChanged(val name: String) : LoginAction
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object TogglePasswordVisibility : LoginAction
    data object LoginClicked : LoginAction

    /** Submits the sign-up form (only meaningful in register mode). */
    data object RegisterClicked : LoginAction

    /** Switches the screen between login and sign-up. */
    data object ToggleAuthMode : LoginAction
    data object GoogleSignInClicked : LoginAction
    data object AppleSignInClicked : LoginAction
    data object ForgotPasswordClicked : LoginAction
}
