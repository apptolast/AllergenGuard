package com.apptolast.menufrontend.data.auth

/**
 * Result of a native social sign-in: the provider id token (+ nonce/name) that the platform UI
 * obtained. The Firebase exchange itself is done in [com.apptolast.menufrontend.data.firebase
 * .FirebaseAuthRepository] via the shared Identity Toolkit `signInWithIdp` REST call, so the social
 * session lands in the same [org.apptolast.menuadmin.data.remote.auth.TokenManager] as email/password.
 */
data class SocialSignInResult(
    // "google.com" or "apple.com".
    val providerId: String,
    val idToken: String,
    // Raw (un-hashed) nonce — Apple only. Null for Google.
    val rawNonce: String? = null,
    // Display name when the provider supplies it (Apple: first sign-in only). Null otherwise.
    val displayName: String? = null,
)

/**
 * Platform abstraction over native social sign-in. Bound per-platform in Koin via
 * `platformModule` (Android = Google through Credential Manager; iOS = Sign in with Apple).
 *
 * Per product scope each platform exposes a single provider: Google on Android, Apple on iOS.
 * The unavailable provider reports `false` (so its button is hidden) and throws if invoked.
 */
interface SocialAuthClient {
    val isGoogleAvailable: Boolean
    val isAppleAvailable: Boolean

    suspend fun signInWithGoogle(): SocialSignInResult
    suspend fun signInWithApple(): SocialSignInResult
}

/** The provider is not configured / not available on this platform. */
class SocialAuthUnavailableException(message: String) : IllegalStateException(message)

/** The user dismissed the native sign-in sheet — treated as a silent no-op (no error shown). */
class SocialAuthCancelledException : Exception("Social sign-in cancelled by the user")
