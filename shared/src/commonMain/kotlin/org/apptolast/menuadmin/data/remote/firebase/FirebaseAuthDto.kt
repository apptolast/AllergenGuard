package org.apptolast.menuadmin.data.remote.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseSignInRequest(
    val email: String,
    val password: String,
    // No default on purpose: kotlinx-serialization omits fields equal to their default, and if
    // `returnSecureToken` is omitted Identity Toolkit returns only an idToken (no refreshToken/expiresIn).
    val returnSecureToken: Boolean,
)

/**
 * Request for Identity Toolkit `accounts:signInWithIdp` — exchanges a federated provider id token
 * (Google / Apple) for a Firebase session. Used by the native social sign-in flows: the platform
 * (Android Credential Manager / iOS Sign in with Apple) obtains the provider id token, then this
 * call turns it into a Firebase idToken + refreshToken stored in the same [TokenManager] as
 * email/password, so the whole app has a single source of truth for the session.
 *
 * None of these fields have a default: kotlinx-serialization omits fields equal to their default
 * (encodeDefaults is off), and Identity Toolkit requires all of them on every request.
 */
@Serializable
data class FirebaseSignInWithIdpRequest(
    // OAuth response encoded as a query string, e.g. "id_token=<JWT>&providerId=apple.com&nonce=<raw>".
    val postBody: String,
    // Any valid URI; Identity Toolkit only needs it to be present for the IdP handshake.
    val requestUri: String,
    val returnSecureToken: Boolean,
    // Surfaces provider-specific data (e.g. INVALID_IDP_RESPONSE details) in the response.
    val returnIdpCredential: Boolean,
)

/** Response from Identity Toolkit signInWithPassword / signUp / signInWithIdp. */
@Serializable
data class FirebaseSignInResponse(
    val idToken: String,
    val refreshToken: String,
    // Identity Toolkit returns expiresIn as a String number of seconds (e.g. "3600").
    val expiresIn: String = "3600",
    val localId: String = "",
    val email: String = "",
    // signInWithIdp only: full name from the provider (Apple sends it on first sign-in only).
    val displayName: String = "",
)

/** Request for Identity Toolkit `accounts:delete` — permanently deletes the signed-in user. */
@Serializable
data class FirebaseDeleteAccountRequest(
    val idToken: String,
)

/**
 * Request for Identity Toolkit `accounts:update` — sets profile fields on the signed-in account.
 * Here it is used to persist the `displayName`; `returnSecureToken` asks for a refreshed session so the
 * caller can immediately pick up the new `name` claim.
 */
@Serializable
data class FirebaseUpdateProfileRequest(
    val idToken: String,
    val displayName: String,
    val returnSecureToken: Boolean,
)

/** Response from the Secure Token refresh endpoint (snake_case fields). */
@Serializable
data class FirebaseRefreshResponse(
    @SerialName("id_token") val idToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: String = "3600",
    @SerialName("user_id") val userId: String = "",
)
