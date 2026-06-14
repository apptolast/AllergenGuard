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
