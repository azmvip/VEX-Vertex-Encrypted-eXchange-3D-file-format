package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "mock_rules")
data class MockRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val urlPattern: String,
    val matchType: String = "contains", // contains, exact, regex
    val isEnabled: Boolean = true,
    val actionType: String = "mock_response", // mock_response, block, inject_headers, redirect
    val mockStatusCode: Int = 200,
    val mockResponseBody: String = "{\"status\": \"mocked\", \"message\": \"ReqInspect Mock Response\"}",
    val mockHeadersJson: String = "{\"Content-Type\": \"application/json\", \"X-Mocked-By\": \"ReqInspect\"}",
    val redirectUrl: String? = null
) {
    fun matches(targetUrl: String): Boolean {
        if (!isEnabled) return false
        return try {
            when (matchType) {
                "contains" -> targetUrl.contains(urlPattern, ignoreCase = true)
                "exact" -> targetUrl.equals(urlPattern, ignoreCase = true)
                "regex" -> Regex(urlPattern).containsMatchIn(targetUrl)
                else -> targetUrl.contains(urlPattern, ignoreCase = true)
            }
        } catch (_: Exception) {
            false
        }
    }

    fun getMockHeadersMap(): Map<String, String> {
        return try {
            val obj = JSONObject(mockHeadersJson)
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
