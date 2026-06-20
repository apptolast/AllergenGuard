package com.apptolast.menufrontend.data.repository

import com.apptolast.menufrontend.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>

    /** Whether each social provider can be offered on this platform (drives button visibility). */
    val isGoogleSignInAvailable: Boolean
    val isAppleSignInAvailable: Boolean

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>

    /** Google Sign-In (Android). Obtains a Google id token natively, exchanges it via Firebase. */
    suspend fun loginWithGoogle(): Result<User>

    /** Sign in with Apple (iOS). Obtains an Apple identity token natively, exchanges it via Firebase. */
    suspend fun loginWithApple(): Result<User>

    suspend fun logout()

    /** Permanently deletes the current account (Identity Toolkit) and clears the local session. */
    suspend fun deleteAccount(): Result<Unit>
}
