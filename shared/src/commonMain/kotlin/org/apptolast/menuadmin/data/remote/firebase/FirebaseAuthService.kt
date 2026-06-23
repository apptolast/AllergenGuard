package org.apptolast.menuadmin.data.remote.firebase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType

/**
 * Firebase Authentication over REST (Identity Toolkit + Secure Token).
 *
 * Replaces the custom-backend [org.apptolast.menuadmin.data.remote.auth.AuthService] when the
 * app runs against Firebase. Pure Ktor, so it works on every target including wasmJs.
 */
class FirebaseAuthService(
    private val client: HttpClient,
) {
    suspend fun signInWithPassword(
        email: String,
        password: String,
    ): FirebaseSignInResponse =
        client.post("${FirebaseConfig.IDENTITY_TOOLKIT}/accounts:signInWithPassword") {
            url { parameters.append("key", FirebaseConfig.apiKey) }
            contentType(ContentType.Application.Json)
            setBody(FirebaseSignInRequest(email = email, password = password, returnSecureToken = true))
        }.body()

    suspend fun signUp(
        email: String,
        password: String,
    ): FirebaseSignInResponse =
        client.post("${FirebaseConfig.IDENTITY_TOOLKIT}/accounts:signUp") {
            url { parameters.append("key", FirebaseConfig.apiKey) }
            contentType(ContentType.Application.Json)
            setBody(FirebaseSignInRequest(email = email, password = password, returnSecureToken = true))
        }.body()

    /**
     * Exchanges a federated provider id token for a Firebase session.
     *
     * @param providerId `"google.com"` or `"apple.com"`.
     * @param idToken the OIDC id token obtained natively (Google ID token / Apple identity token).
     * @param rawNonce the *raw* (un-hashed) nonce used by Sign in with Apple. The Apple request was
     *   signed with `sha256(rawNonce)`; Identity Toolkit re-hashes this value and compares it with
     *   the token's `nonce` claim. Pass `null` for Google (no nonce).
     */
    suspend fun signInWithIdp(
        providerId: String,
        idToken: String,
        rawNonce: String? = null,
    ): FirebaseSignInResponse {
        val postBody = buildString {
            append("id_token=").append(idToken)
            append("&providerId=").append(providerId)
            if (!rawNonce.isNullOrBlank()) append("&nonce=").append(rawNonce)
        }
        return client.post("${FirebaseConfig.IDENTITY_TOOLKIT}/accounts:signInWithIdp") {
            url { parameters.append("key", FirebaseConfig.apiKey) }
            contentType(ContentType.Application.Json)
            setBody(
                FirebaseSignInWithIdpRequest(
                    postBody = postBody,
                    requestUri = "http://localhost",
                    returnSecureToken = true,
                    returnIdpCredential = true,
                ),
            )
        }.body()
    }

    /**
     * Sets the `displayName` on the account that owns [idToken] (Identity Toolkit `accounts:update`).
     * Returns the refreshed session (new idToken/refreshToken) so the caller can store the updated claims.
     */
    suspend fun updateProfile(
        idToken: String,
        displayName: String,
    ): FirebaseSignInResponse =
        client.post("${FirebaseConfig.IDENTITY_TOOLKIT}/accounts:update") {
            url { parameters.append("key", FirebaseConfig.apiKey) }
            contentType(ContentType.Application.Json)
            setBody(
                FirebaseUpdateProfileRequest(
                    idToken = idToken,
                    displayName = displayName,
                    returnSecureToken = true,
                ),
            )
        }.body()

    /** Permanently deletes the account that owns [idToken] (Identity Toolkit `accounts:delete`). */
    suspend fun deleteAccount(idToken: String) {
        client.post("${FirebaseConfig.IDENTITY_TOOLKIT}/accounts:delete") {
            url { parameters.append("key", FirebaseConfig.apiKey) }
            contentType(ContentType.Application.Json)
            setBody(FirebaseDeleteAccountRequest(idToken))
        }
    }

    suspend fun refreshIdToken(refreshToken: String): FirebaseRefreshResponse =
        client.submitForm(
            url = "${FirebaseConfig.SECURE_TOKEN}/token",
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
            },
        ) {
            url { parameters.append("key", FirebaseConfig.apiKey) }
        }.body()
}
