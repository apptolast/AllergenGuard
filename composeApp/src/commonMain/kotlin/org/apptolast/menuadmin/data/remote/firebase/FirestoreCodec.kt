package org.apptolast.menuadmin.data.remote.firebase

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Encodes/decodes Firestore REST typed values (`{"stringValue": ...}`, `{"arrayValue": ...}`, etc.)
 * to/from plain Kotlin values (String, Long, Double, Boolean, List, Map, null). This is the same
 * document shape used to seed the database, so encoders and the stored data stay in sync.
 */
object FirestoreCodec {
    private val EMPTY = JsonObject(emptyMap())

    fun decodeFields(fields: JsonObject): Map<String, Any?> = fields.mapValues { decode(it.value) }

    fun encodeFields(map: Map<String, Any?>): JsonObject =
        buildJsonObject { map.forEach { (k, v) -> put(k, encode(v)) } }

    fun decode(value: JsonElement): Any? {
        val obj = value.jsonObject
        return when (obj.keys.firstOrNull()) {
            "stringValue" -> obj["stringValue"]!!.jsonPrimitive.content
            "integerValue" -> obj["integerValue"]!!.jsonPrimitive.content.toLong()
            "doubleValue" -> obj["doubleValue"]!!.jsonPrimitive.double
            "booleanValue" -> obj["booleanValue"]!!.jsonPrimitive.boolean
            "timestampValue" -> obj["timestampValue"]!!.jsonPrimitive.content
            "nullValue" -> null
            "mapValue" -> decodeMap(obj)
            "arrayValue" -> decodeArray(obj)
            else -> null
        }
    }

    private fun decodeMap(obj: JsonObject): Map<String, Any?> =
        (obj["mapValue"]!!.jsonObject["fields"]?.jsonObject ?: EMPTY).mapValues { decode(it.value) }

    private fun decodeArray(obj: JsonObject): List<Any?> =
        (obj["arrayValue"]!!.jsonObject["values"]?.jsonArray ?: JsonArray(emptyList())).map { decode(it) }

    fun encode(v: Any?): JsonObject =
        when (v) {
            null -> buildJsonObject { put("nullValue", JsonNull) }
            is String -> buildJsonObject { put("stringValue", v) }
            is Boolean -> buildJsonObject { put("booleanValue", v) }
            is Int -> buildJsonObject { put("integerValue", v.toString()) }
            is Long -> buildJsonObject { put("integerValue", v.toString()) }
            is Double -> buildJsonObject { put("doubleValue", v) }
            is Map<*, *> -> encodeMap(v)
            is List<*> -> encodeArray(v)
            else -> buildJsonObject { put("stringValue", v.toString()) }
        }

    private fun encodeMap(v: Map<*, *>): JsonObject =
        buildJsonObject {
            put(
                "mapValue",
                buildJsonObject {
                    put("fields", buildJsonObject { v.forEach { (k, vv) -> put(k.toString(), encode(vv)) } })
                },
            )
        }

    private fun encodeArray(v: List<*>): JsonObject =
        buildJsonObject {
            put(
                "arrayValue",
                buildJsonObject {
                    if (v.isNotEmpty()) put("values", buildJsonArray { v.forEach { add(encode(it)) } })
                },
            )
        }
}
