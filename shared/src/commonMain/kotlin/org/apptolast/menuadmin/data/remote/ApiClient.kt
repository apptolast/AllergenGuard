package org.apptolast.menuadmin.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apptolast.menuadmin.data.remote.auth.AuthResponseDto
import org.apptolast.menuadmin.data.remote.auth.RefreshTokenRequestDto
import org.apptolast.menuadmin.data.remote.auth.TokenManager

class ApiException(
    val statusCode: Int,
    override val message: String,
) : Exception("HTTP $statusCode: $message")

/**
 * HttpClient for unauthenticated endpoints (login, register).
 * No auth interceptor — safe to use before tokens exist.
 */
fun createAuthHttpClient(json: Json): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.HEADERS
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    parseErrorResponse(response.bodyAsText(), response.status.value)
                }
            }
        }

        defaultRequest {
            url(ApiConstants.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
}

/**
 * HttpClient for authenticated API calls.
 * Manages Bearer tokens via HttpSend interceptor:
 * - Proactively refreshes expired tokens before each request
 * - Retries once on 403 with a fresh token (handles backend returning
 *   403 instead of 401 for expired/invalid tokens)
 */
fun createHttpClient(
    tokenManager: TokenManager,
    json: Json,
): HttpClient {
    // Lightweight client for token refresh calls (no auth interceptor)
    val refreshClient = HttpClient {
        install(ContentNegotiation) { json(json) }
        defaultRequest {
            url(ApiConstants.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }

    val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.HEADERS
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    parseErrorResponse(response.bodyAsText(), response.status.value)
                }
            }
        }

        defaultRequest {
            url(ApiConstants.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }

    client.plugin(HttpSend).intercept { request ->
        // Proactively refresh if token is expired or about to expire
        if (tokenManager.isAccessTokenExpired() && tokenManager.refreshToken != null) {
            tryRefreshToken(tokenManager, refreshClient)
        }

        // Add Authorization header
        tokenManager.accessToken?.let { token ->
            request.headers {
                remove(HttpHeaders.Authorization)
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        val originalCall = execute(request)

        // On 403, attempt one token refresh + retry
        if (originalCall.response.status == HttpStatusCode.Forbidden &&
            tokenManager.refreshToken != null
        ) {
            val refreshed = tryRefreshToken(tokenManager, refreshClient)
            if (refreshed) {
                tokenManager.accessToken?.let { newToken ->
                    request.headers {
                        remove(HttpHeaders.Authorization)
                        append(HttpHeaders.Authorization, "Bearer $newToken")
                    }
                }
                execute(request)
            } else {
                originalCall
            }
        } else {
            originalCall
        }
    }

    return client
}

private suspend fun tryRefreshToken(
    tokenManager: TokenManager,
    refreshClient: HttpClient,
): Boolean {
    val refresh = tokenManager.refreshToken ?: return false
    return try {
        val response: AuthResponseDto = refreshClient.post(ApiConstants.AUTH_REFRESH) {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = refresh))
        }.body()
        tokenManager.saveTokens(
            response.accessToken,
            response.refreshToken,
            response.expiresIn,
        )
        true
    } catch (_: Exception) {
        tokenManager.clearTokens()
        false
    }
}

private fun parseErrorResponse(
    body: String,
    statusCode: Int,
): Nothing {
    val message = try {
        val jsonElement = Json.parseToJsonElement(body).jsonObject
        // Backend wraps errors as {error: {code, message, status, ...}}
        val errorObj = jsonElement["error"]?.jsonObject
        errorObj?.get("message")?.jsonPrimitive?.content
            ?: jsonElement["message"]?.jsonPrimitive?.content
            ?: jsonElement["error"]?.jsonPrimitive?.content
            ?: body
    } catch (_: Exception) {
        body
    }
    throw ApiException(statusCode, message)
}
