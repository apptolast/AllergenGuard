package com.apptolast.menufrontend.data.firebase

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
 */
class FirebaseAuthRepository(
    private val authService: FirebaseAuthService,
    private val tokenManager: TokenManager,
) : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

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

    override suspend fun loginWithGoogle(): Result<User> =
        Result.failure(UnsupportedOperationException("Google sign-in requires platform Google auth (not wired yet)"))

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
