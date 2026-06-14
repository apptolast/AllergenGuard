package org.apptolast.menuadmin.data.remote.auth

import com.russhwolf.settings.Settings
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TokenManager(
    private val settings: Settings = Settings(),
) {
    var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (value != null) {
                settings.putString(KEY_ACCESS_TOKEN, value)
            } else {
                settings.remove(KEY_ACCESS_TOKEN)
            }
        }

    var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)
        set(value) {
            if (value != null) {
                settings.putString(KEY_REFRESH_TOKEN, value)
            } else {
                settings.remove(KEY_REFRESH_TOKEN)
            }
        }

    var expiresIn: Long
        get() = settings.getLong(KEY_EXPIRES_IN, 0L)
        set(value) = settings.putLong(KEY_EXPIRES_IN, value)

    private var accessTokenExpiresAt: Long
        get() = settings.getLong(KEY_EXPIRES_AT, 0L)
        set(value) = settings.putLong(KEY_EXPIRES_AT, value)

    val isLoggedIn: Boolean get() = accessToken != null

    fun isAccessTokenExpired(): Boolean {
        val expiresAt = accessTokenExpiresAt
        if (expiresAt == 0L) return accessToken == null
        // Refresh 30 seconds before actual expiry
        return Clock.System.now().toEpochMilliseconds() >= (expiresAt - REFRESH_BUFFER_MS)
    }

    fun saveTokens(
        access: String,
        refresh: String,
        expires: Long,
    ) {
        accessToken = access
        refreshToken = refresh
        expiresIn = expires
        accessTokenExpiresAt = Clock.System.now().toEpochMilliseconds() + (expires * 1000)
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_EXPIRES_IN)
        settings.remove(KEY_EXPIRES_AT)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_IN = "expires_in"
        const val KEY_EXPIRES_AT = "expires_at"
        const val REFRESH_BUFFER_MS = 30_000L
    }
}
