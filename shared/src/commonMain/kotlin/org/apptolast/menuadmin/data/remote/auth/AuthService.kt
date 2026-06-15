package org.apptolast.menuadmin.data.remote.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.apptolast.menuadmin.data.remote.ApiConstants

class AuthService(
    private val client: HttpClient,
) {
    suspend fun login(
        email: String,
        password: String,
    ): AuthResponseDto =
        client.post(ApiConstants.AUTH_LOGIN) {
            setBody(LoginRequestDto(email = email, password = password))
        }.body()

    suspend fun registerAdmin(
        email: String,
        password: String,
        name: String? = null,
    ): AuthResponseDto =
        client.post(ApiConstants.AUTH_REGISTER_ADMIN) {
            setBody(
                RegisterAdminRequestDto(
                    email = email,
                    password = password,
                    name = name,
                ),
            )
        }.body()

    suspend fun refreshToken(refreshToken: String): AuthResponseDto =
        client.post(ApiConstants.AUTH_REFRESH) {
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }.body()
}
