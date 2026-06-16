package com.apptolast.menufrontend.data.auth

import com.apptolast.menufrontend.data.auth.IosAppleAuthBridge.signInHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Completion the Swift coordinator calls with `"idToken|||rawNonce|||displayName"`, or null on cancel/error. */
typealias IosAppleCompletion = (String?) -> Unit

/**
 * Bridge between Kotlin and the Swift Sign in with Apple flow. Swift installs [signInHandler] at app
 * launch (`SocialAuthCoordinator.registerBridges()` in `iOSApp.swift`); when invoked it drives an
 * `ASAuthorizationController` and calls the completion back with the Apple identity token + raw nonce.
 */
object IosAppleAuthBridge {
    var signInHandler: ((IosAppleCompletion) -> Unit)? = null
}

/**
 * iOS social sign-in. Per product scope only Apple is offered here (Google lives on Android). The
 * native flow yields the Apple identity token + raw nonce; the Firebase exchange happens in the
 * shared REST layer (`signInWithIdp` with `providerId = "apple.com"`).
 */
class IosSocialAuthClient : SocialAuthClient {
    override val isGoogleAvailable: Boolean = false
    override val isAppleAvailable: Boolean get() = IosAppleAuthBridge.signInHandler != null

    override suspend fun signInWithGoogle(): SocialSignInResult =
        throw SocialAuthUnavailableException("Google Sign-In solo está disponible en Android.")

    override suspend fun signInWithApple(): SocialSignInResult {
        val handler = IosAppleAuthBridge.signInHandler
            ?: throw SocialAuthUnavailableException("Sign in with Apple no está disponible.")

        val payload = suspendCancellableCoroutine<String?> { cont ->
            handler { result -> cont.resume(result) }
        } ?: throw SocialAuthCancelledException()

        val parts = payload.split(SEPARATOR)
        val idToken = parts.getOrNull(0).orEmpty()
        val rawNonce = parts.getOrNull(1).orEmpty()
        val displayName = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
        if (idToken.isBlank() || rawNonce.isBlank()) {
            throw SocialAuthUnavailableException("Apple no devolvió una credencial válida.")
        }
        return SocialSignInResult(
            providerId = "apple.com",
            idToken = idToken,
            rawNonce = rawNonce,
            displayName = displayName,
        )
    }

    private companion object {
        const val SEPARATOR = "|||"
    }
}
