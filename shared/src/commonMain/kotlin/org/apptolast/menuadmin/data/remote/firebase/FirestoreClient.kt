package org.apptolast.menuadmin.data.remote.firebase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.time.Instant

class FirestoreException(
    val statusCode: Int,
    override val message: String,
) : Exception("Firestore $statusCode: $message")

data class FirestoreDocument(
    val id: String,
    val fields: Map<String, Any?>,
    // Server-managed timestamps returned by Firestore REST (RFC-3339). Used as the source of truth
    // for "created/updated" since the app does not store its own timestamp fields.
    val createTime: Instant? = null,
    val updateTime: Instant? = null,
)

/**
 * Thin Firestore REST data-plane client. Works on every target (incl. wasmJs) because it only
 * uses Ktor. The injected [client] is expected to attach a Firebase ID token as Bearer.
 *
 * Paths are relative to the default database documents root, e.g. `"ingredients"` or
 * `"restaurants/{id}/recipes"` for collections, and `"ingredients/{id}"` for a document.
 */
class FirestoreClient(
    private val client: HttpClient,
) {
    private val base = FirebaseConfig.firestoreDocuments

    suspend fun listDocuments(collectionPath: String): List<FirestoreDocument> {
        val out = mutableListOf<FirestoreDocument>()
        var pageToken: String? = null
        do {
            val resp = client.get("$base/$collectionPath") {
                parameter("pageSize", 300)
                pageToken?.let { parameter("pageToken", it) }
            }
            if (!resp.status.isSuccess()) throw FirestoreException(resp.status.value, resp.bodyAsText())
            val body = resp.body<JsonObject>()
            body["documents"]?.jsonArray?.forEach { out += parseDoc(it.jsonObject) }
            pageToken = body["nextPageToken"]?.jsonPrimitive?.contentOrNull
        } while (pageToken != null)
        return out
    }

    suspend fun getDocument(path: String): FirestoreDocument? {
        val resp = client.get("$base/$path")
        if (resp.status == HttpStatusCode.NotFound) return null
        if (!resp.status.isSuccess()) throw FirestoreException(resp.status.value, resp.bodyAsText())
        return parseDoc(resp.body<JsonObject>())
    }

    /**
     * Creates or updates the document at [path] (idempotent upsert). With [updateMask] only the
     * listed field paths are written, leaving any other existing fields intact (e.g. server-managed
     * `rating`); without it the document fields are fully replaced.
     */
    suspend fun patchDocument(
        path: String,
        fields: Map<String, Any?>,
        updateMask: List<String>? = null,
    ): FirestoreDocument {
        val resp = client.patch("$base/$path") {
            updateMask?.forEach { parameter("updateMask.fieldPaths", it) }
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("fields", FirestoreCodec.encodeFields(fields)) })
        }
        if (!resp.status.isSuccess()) throw FirestoreException(resp.status.value, resp.bodyAsText())
        return parseDoc(resp.body<JsonObject>())
    }

    suspend fun deleteDocument(path: String) {
        val resp = client.delete("$base/$path")
        if (!resp.status.isSuccess() && resp.status != HttpStatusCode.NotFound) {
            throw FirestoreException(resp.status.value, resp.bodyAsText())
        }
    }

    private fun parseDoc(obj: JsonObject): FirestoreDocument {
        val name = obj["name"]?.jsonPrimitive?.content ?: ""
        val fields = obj["fields"]?.jsonObject ?: JsonObject(emptyMap())
        return FirestoreDocument(
            id = name.substringAfterLast('/'),
            fields = FirestoreCodec.decodeFields(fields),
            createTime = obj["createTime"]?.jsonPrimitive?.contentOrNull?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            },
            updateTime = obj["updateTime"]?.jsonPrimitive?.contentOrNull?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            },
        )
    }
}
