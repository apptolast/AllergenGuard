package org.apptolast.menuadmin.data.remote.firebase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Reads a claim from a Firebase ID token (JWT). The signature is NOT verified — this is only used
 * client-side to recover claims the client itself needs to send back (e.g. `accountId` for the
 * Firestore rules). Security is enforced server-side by the rules against the verified token.
 */
object FirebaseIdToken {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalEncodingApi::class)
    fun claim(
        idToken: String?,
        name: String,
    ): String? {
        val parts = idToken?.split('.') ?: return null
        if (parts.size < 2) return null
        return try {
            var payload = parts[1]
            while (payload.length % 4 != 0) payload += "="
            val decoded = Base64.UrlSafe.decode(payload).decodeToString()
            json.parseToJsonElement(decoded).jsonObject[name]?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }
}
