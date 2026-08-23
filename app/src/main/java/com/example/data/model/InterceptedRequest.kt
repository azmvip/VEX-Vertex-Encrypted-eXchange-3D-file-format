package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "intercepted_requests")
data class InterceptedRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String = "default",
    val timestamp: Long = System.currentTimeMillis(),
    val url: String,
    val method: String = "GET",
    val statusCode: Int = 200,
    val statusText: String = "OK",
    val resourceType: String = "fetch", // fetch, xhr, document, script, stylesheet, image, media, font, websocket, other
    val requestHeadersJson: String = "{}",
    val responseHeadersJson: String = "{}",
    val queryParamsJson: String = "{}",
    val requestBody: String? = null,
    val responseBody: String? = null,
    val contentType: String? = null,
    val contentLength: Long = 0,
    val durationMs: Long = 0,
    val isBlocked: Boolean = false,
    val isMocked: Boolean = false,
    val isBookmarked: Boolean = false,
    val errorMessage: String? = null,
    val initiator: String? = null
) {
    val host: String
        get() = try {
            val uri = java.net.URI(url)
            uri.host ?: url.take(30)
        } catch (_: Exception) {
            url.take(30)
        }

    val path: String
        get() = try {
            val uri = java.net.URI(url)
            val p = uri.path ?: "/"
            if (uri.query != null) "$p?${uri.query}" else p
        } catch (_: Exception) {
            url
        }

    fun getRequestHeadersMap(): Map<String, String> {
        return parseJsonMap(requestHeadersJson)
    }

    fun getResponseHeadersMap(): Map<String, String> {
        return parseJsonMap(responseHeadersJson)
    }

    fun getQueryParamsMap(): Map<String, String> {
        return parseJsonMap(queryParamsJson)
    }

    private fun parseJsonMap(json: String): Map<String, String> {
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { key ->
                map[key] = obj.optString(key, "")
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
