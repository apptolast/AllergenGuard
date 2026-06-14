package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.firebase.FirebaseAuthService
import org.apptolast.menuadmin.domain.repository.AuthRepository

/**
 * [AuthRepository] backed by Firebase Authentication (Identity Toolkit REST). Stores the Firebase
 * ID token + refresh token in [TokenManager] so the Firestore client can authenticate.
 */
class FirebaseAuthRepository(
    private val authService: FirebaseAuthService,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override val isLoggedIn: Boolean get() = tokenManager.isLoggedIn

    override suspend fun login(
        email: String,
        password: String,
    ) {
        val r = authService.signInWithPassword(email, password)
        tokenManager.saveTokens(r.idToken, r.refreshToken, r.expiresIn.toLongOrNull() ?: 3600L)
    }

    override suspend fun registerAdmin(
        email: String,
        password: String,
        name: String?,
    ) {
        val r = authService.signUp(email, password)
        tokenManager.saveTokens(r.idToken, r.refreshToken, r.expiresIn.toLongOrNull() ?: 3600L)
    }

    override fun logout() {
        tokenManager.clearTokens()
    }
}
