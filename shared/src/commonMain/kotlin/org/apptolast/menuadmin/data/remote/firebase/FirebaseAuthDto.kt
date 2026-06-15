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

/** Response from Identity Toolkit signInWithPassword / signUp. */
@Serializable
data class FirebaseSignInResponse(
    val idToken: String,
    val refreshToken: String,
    // Identity Toolkit returns expiresIn as a String number of seconds (e.g. "3600").
    val expiresIn: String = "3600",
    val localId: String = "",
    val email: String = "",
)

/** Response from the Secure Token refresh endpoint (snake_case fields). */
@Serializable
data class FirebaseRefreshResponse(
    @SerialName("id_token") val idToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: String = "3600",
    @SerialName("user_id") val userId: String = "",
)
