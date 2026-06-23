package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.auth.AuthService
import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.firebase.FirebaseIdToken
import org.apptolast.menuadmin.domain.repository.AuthRepository

class RemoteAuthRepository(
    private val authService: AuthService,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override val isLoggedIn: Boolean get() = tokenManager.isLoggedIn

    // Best-effort claim decode from the JWT access token (FirebaseIdToken is a generic JWT reader).
    override val currentUserEmail: String?
        get() = FirebaseIdToken.claim(tokenManager.accessToken, "email")
            ?: FirebaseIdToken.claim(tokenManager.accessToken, "sub")
    override val currentUserId: String?
        get() = FirebaseIdToken.claim(tokenManager.accessToken, "sub")
    override val currentUserName: String?
        get() = FirebaseIdToken.claim(tokenManager.accessToken, "name")?.takeIf { it.isNotBlank() }
    override val isEmailVerified: Boolean get() = false

    override suspend fun login(
        email: String,
        password: String,
    ) {
        val response = authService.login(email, password)
        tokenManager.saveTokens(response.accessToken, response.refreshToken, response.expiresIn)
    }

    override suspend fun registerAdmin(
        email: String,
        password: String,
        name: String?,
    ) {
        val response = authService.registerAdmin(email, password, name)
        tokenManager.saveTokens(response.accessToken, response.refreshToken, response.expiresIn)
    }

    override fun logout() {
        tokenManager.clearTokens()
    }
}
