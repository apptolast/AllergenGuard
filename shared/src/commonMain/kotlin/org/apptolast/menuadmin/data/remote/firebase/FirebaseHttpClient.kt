package org.apptolast.menuadmin.data.remote.firebase

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.data.remote.auth.TokenManager

/**
 * Authenticated HttpClient for Firestore REST. Attaches the stored Firebase ID token as Bearer and
 * refreshes it via the Secure Token endpoint when it is expired (proactively) or when Firestore
 * answers 401. Mirrors the backend [org.apptolast.menuadmin.data.remote.createHttpClient] but for Firebase.
 */
fun createFirestoreHttpClient(
    tokenManager: TokenManager,
    authService: FirebaseAuthService,
    json: Json,
): HttpClient {
    val client = HttpClient {
        install(ContentNegotiation) { json(json) }
        expectSuccess = false
    }

    client.plugin(HttpSend).intercept { request ->
        if (tokenManager.isAccessTokenExpired() && tokenManager.refreshToken != null) {
            refreshFirebaseToken(tokenManager, authService)
        }
        tokenManager.accessToken?.let { token ->
            request.headers {
                remove(HttpHeaders.Authorization)
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        val call = execute(request)
        if (call.response.status == HttpStatusCode.Unauthorized && tokenManager.refreshToken != null) {
            if (refreshFirebaseToken(tokenManager, authService)) {
                tokenManager.accessToken?.let { token ->
                    request.headers {
                        remove(HttpHeaders.Authorization)
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
                execute(request)
            } else {
                call
            }
        } else {
            call
        }
    }
    return client
}

private suspend fun refreshFirebaseToken(
    tokenManager: TokenManager,
    authService: FirebaseAuthService,
): Boolean {
    val refresh = tokenManager.refreshToken ?: return false
    return try {
        val r = authService.refreshIdToken(refresh)
        tokenManager.saveTokens(r.idToken, r.refreshToken, r.expiresIn.toLongOrNull() ?: 3600L)
        true
    } catch (_: Exception) {
        tokenManager.clearTokens()
        false
    }
}
