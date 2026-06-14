package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.auth.AuthResponseDto
import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.firebase.FirebaseAuthService
import org.apptolast.menuadmin.domain.repository.AuthRepository

/**
 * [AuthRepository] backed by Firebase Authentication (Identity Toolkit REST). Stores the Firebase
 * ID token + refresh token in [TokenManager] so the Firestore client can authenticate. Returns the
 * existing [AuthResponseDto] shape so callers (ViewModels) are unchanged.
 */
class FirebaseAuthRepository(
    private val authService: FirebaseAuthService,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override val isLoggedIn: Boolean get() = tokenManager.isLoggedIn

    override suspend fun login(
        email: String,
        password: String,
    ): AuthResponseDto {
        val r = authService.signInWithPassword(email, password)
        val expires = r.expiresIn.toLongOrNull() ?: 3600L
        tokenManager.saveTokens(r.idToken, r.refreshToken, expires)
        return AuthResponseDto(accessToken = r.idToken, refreshToken = r.refreshToken, expiresIn = expires)
    }

    override suspend fun registerAdmin(
        email: String,
        password: String,
        name: String?,
    ): AuthResponseDto {
        val r = authService.signUp(email, password)
        val expires = r.expiresIn.toLongOrNull() ?: 3600L
        tokenManager.saveTokens(r.idToken, r.refreshToken, expires)
        return AuthResponseDto(accessToken = r.idToken, refreshToken = r.refreshToken, expiresIn = expires)
    }

    override fun logout() {
        tokenManager.clearTokens()
    }
}
