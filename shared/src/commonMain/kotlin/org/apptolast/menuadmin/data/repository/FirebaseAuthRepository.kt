package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.firebase.FirebaseAuthService
import org.apptolast.menuadmin.data.remote.firebase.FirebaseIdToken
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

    // Decoded from the Firebase ID token's claims (no signature check needed for display).
    override val currentUserEmail: String?
        get() = FirebaseIdToken.claim(tokenManager.accessToken, "email")
    override val currentUserId: String?
        get() = FirebaseIdToken.claim(tokenManager.accessToken, "user_id")
    override val currentUserName: String?
        get() = FirebaseIdToken.claim(tokenManager.accessToken, "name")?.takeIf { it.isNotBlank() }
    override val isEmailVerified: Boolean
        get() = FirebaseIdToken.claim(tokenManager.accessToken, "email_verified") == "true"

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
        // Persist the display name, then refresh the session so the new `name` claim is available.
        val displayName = name?.trim()
        if (!displayName.isNullOrBlank()) {
            runCatching {
                authService.updateProfile(r.idToken, displayName)
                val refreshed = authService.refreshIdToken(r.refreshToken)
                tokenManager.saveTokens(
                    refreshed.idToken,
                    refreshed.refreshToken,
                    refreshed.expiresIn.toLongOrNull() ?: 3600L,
                )
            }
        }
    }

    override suspend fun updateDisplayName(name: String) {
        val token = tokenManager.accessToken ?: error("No hay sesión activa")
        // Persist the display name server-side, then refresh the session so the local idToken carries
        // the updated `name` claim (accounts:update itself does not reissue tokens).
        authService.updateProfile(token, name.trim())
        tokenManager.refreshToken?.takeIf { it.isNotBlank() }?.let { refresh ->
            val r = authService.refreshIdToken(refresh)
            tokenManager.saveTokens(r.idToken, r.refreshToken, r.expiresIn.toLongOrNull() ?: 3600L)
        }
    }

    override fun logout() {
        tokenManager.clearTokens()
    }
}
