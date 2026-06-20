package org.apptolast.menuadmin.data.remote.firebase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Thin Firebase Storage REST client (the `v0` endpoint, parallel to [FirestoreClient]). Uploads raw
 * bytes and returns a public download URL of the `?alt=media&token=` form — the same one the Firebase
 * SDK / console produce, so the object previews correctly there.
 *
 * The injected [client] attaches the Firebase ID token as Bearer, so Storage security rules apply.
 */
class FirebaseStorageClient(
    private val client: HttpClient,
) {
    private val base = "${FirebaseConfig.STORAGE}/v0/b/${FirebaseConfig.storageBucket}/o"

    /**
     * Uploads [bytes] to the object [path] (e.g. `dish-images/{restaurantId}/{id}.webp`) with the
     * given [contentType] and returns its public download URL.
     */
    suspend fun uploadBytes(
        path: String,
        bytes: ByteArray,
        contentType: String,
    ): String {
        val resp = client.post(base) {
            parameter("name", path)
            contentType(ContentType.parse(contentType))
            setBody(bytes)
        }
        if (!resp.status.isSuccess()) throw FirestoreException(resp.status.value, resp.bodyAsText())
        val token = resp.body<JsonObject>()["downloadTokens"]?.jsonPrimitive?.contentOrNull
            ?.substringBefore(",")
        val encodedPath = path.encodeURLParameter()
        return buildString {
            append("$base/$encodedPath?alt=media")
            if (!token.isNullOrEmpty()) append("&token=$token")
        }
    }
}
