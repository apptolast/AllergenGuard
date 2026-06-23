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
        // Persist the display name on the new account so the `name` claim is available afterwards.
        // accounts:update returns a refreshed session, so re-store it to pick up the name immediately.
        val displayName = name?.trim()
        if (!displayName.isNullOrBlank()) {
            runCatching { authService.updateProfile(r.idToken, displayName) }.getOrNull()?.let { u ->
                tokenManager.saveTokens(
                    u.idToken.ifBlank { r.idToken },
                    u.refreshToken.ifBlank { r.refreshToken },
                    u.expiresIn.toLongOrNull() ?: 3600L,
                )
            }
        }
    }

    override fun logout() {
        tokenManager.clearTokens()
    }
}
