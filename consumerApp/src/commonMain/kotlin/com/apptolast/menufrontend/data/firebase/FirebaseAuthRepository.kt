package com.apptolast.menufrontend.data.firebase

import com.apptolast.menufrontend.data.auth.SocialAuthClient
import com.apptolast.menufrontend.data.auth.SocialSignInResult
import com.apptolast.menufrontend.data.repository.AuthRepository
import com.apptolast.menufrontend.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.firebase.FirebaseAuthService
import org.apptolast.menuadmin.data.remote.firebase.FirebaseIdToken

/**
 * Consumer [AuthRepository] backed by Firebase Authentication (Identity Toolkit REST), reusing the
 * shared [FirebaseAuthService] + [TokenManager]. Restores the session from a stored token on start.
 *
 * Social sign-in (Google on Android, Apple on iOS) goes through [socialAuthClient] to obtain the
 * provider id token natively, then through the same REST layer (`signInWithIdp`) + [TokenManager],
 * so the whole app keeps a single session source of truth regardless of how the user logged in.
 */
class FirebaseAuthRepository(
    private val authService: FirebaseAuthService,
    private val tokenManager: TokenManager,
    private val socialAuthClient: SocialAuthClient,
) : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override val isGoogleSignInAvailable: Boolean get() = socialAuthClient.isGoogleAvailable
    override val isAppleSignInAvailable: Boolean get() = socialAuthClient.isAppleAvailable

    init {
        val token = tokenManager.accessToken
        if (token != null && !tokenManager.isAccessTokenExpired()) {
            _currentUser.value = userFromToken(token)
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<User> = runCatching {
        val r = authService.signInWithPassword(email, password)
        tokenManager.saveTokens(r.idToken, r.refreshToken, r.expiresIn.toLongOrNull() ?: 3600L)
        User(id = r.localId, name = r.email.substringBefore("@"), email = r.email.ifEmpty { email })
            .also { _currentUser.value = it }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
    ): Result<User> = runCatching {
        val r = authService.signUp(email, password)
        tokenManager.saveTokens(r.idToken, r.refreshToken, r.expiresIn.toLongOrNull() ?: 3600L)
        User(id = r.localId, name = name, email = r.email.ifEmpty { email })
            .also { _currentUser.value = it }
    }

    override suspend fun loginWithGoogle(): Result<User> = runCatching {
        exchangeIdp(socialAuthClient.signInWithGoogle())
    }

    override suspend fun loginWithApple(): Result<User> = runCatching {
        exchangeIdp(socialAuthClient.signInWithApple())
    }

    /** Turns a native provider id token into a Firebase session stored in [TokenManager]. */
    private suspend fun exchangeIdp(social: SocialSignInResult): User {
        val r = authService.signInWithIdp(social.providerId, social.idToken, social.rawNonce)
        tokenManager.saveTokens(r.idToken, r.refreshToken, r.expiresIn.toLongOrNull() ?: 3600L)
        val email = r.email.ifEmpty { FirebaseIdToken.claim(r.idToken, "email").orEmpty() }
        val name = social.displayName?.takeIf { it.isNotBlank() }
            ?: r.displayName.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").ifBlank { "Usuario" }
        return User(id = r.localId, name = name, email = email)
            .also { _currentUser.value = it }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
        _currentUser.value = null
    }

    private fun userFromToken(token: String): User? {
        val uid = FirebaseIdToken.claim(token, "user_id") ?: FirebaseIdToken.claim(token, "sub") ?: return null
        val email = FirebaseIdToken.claim(token, "email") ?: ""
        return User(id = uid, name = email.substringBefore("@"), email = email)
    }
}
