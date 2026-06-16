package com.apptolast.menufrontend.features.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.menufrontend.data.auth.SocialAuthCancelledException
import com.apptolast.menufrontend.data.repository.AuthRepository
import com.apptolast.menufrontend.features.login.data.LoginAction
import com.apptolast.menufrontend.features.login.data.LoginState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data object NavigateToRegister : LoginEffect
}

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LoginState(
            isGoogleAvailable = authRepository.isGoogleSignInAvailable,
            isAppleAvailable = authRepository.isAppleSignInAvailable,
        ),
    )
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> _state.update { it.copy(email = action.email, error = null) }
            is LoginAction.PasswordChanged -> _state.update { it.copy(password = action.password, error = null) }
            LoginAction.TogglePasswordVisibility -> _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            LoginAction.LoginClicked -> login()
            LoginAction.GoogleSignInClicked -> socialLogin { authRepository.loginWithGoogle() }
            LoginAction.AppleSignInClicked -> socialLogin { authRepository.loginWithApple() }
            LoginAction.RegisterClicked -> viewModelScope.launch { _effect.emit(LoginEffect.NavigateToRegister) }
            LoginAction.ForgotPasswordClicked -> { /* TODO */ }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(_state.value.email, _state.value.password)
                .onSuccess { _effect.emit(LoginEffect.NavigateToHome) }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun socialLogin(signIn: suspend () -> Result<*>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            signIn()
                .onSuccess { _effect.emit(LoginEffect.NavigateToHome) }
                .onFailure { e ->
                    // User dismissed the native sheet → silent, no error banner.
                    if (e !is SocialAuthCancelledException) {
                        _state.update { it.copy(error = e.message) }
                    }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
