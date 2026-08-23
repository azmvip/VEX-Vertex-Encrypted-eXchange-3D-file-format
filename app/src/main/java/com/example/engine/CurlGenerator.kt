package com.example.engine

import com.example.data.model.InterceptedRequestEntity
import java.net.URI

object CurlGenerator {

    fun generateCurl(request: InterceptedRequestEntity): String {
        val sb = StringBuilder("curl")
        
        // Method
        if (request.method.uppercase() != "GET") {
            sb.append(" -X ").append(request.method.uppercase())
        }

        // URL
        sb.append(" '").append(request.url).append("'")

        // Headers
        val headers = request.getRequestHeadersMap()
        for ((key, value) in headers) {
            // Filter pseudo headers or empty headers
            if (!key.startsWith(":") && key.isNotBlank()) {
                val escapedValue = value.replace("'", "'\\''")
                sb.append(" \\\n  -H '").append(key).append(": ").append(escapedValue).append("'")
            }
        }

        // Body
        if (!request.requestBody.isNullOrBlank() && request.method.uppercase() in listOf("POST", "PUT", "PATCH", "DELETE")) {
            val escapedBody = request.requestBody.replace("'", "'\\''")
            sb.append(" \\\n  --data-raw '").append(escapedBody).append("'")
        }

        sb.append(" \\\n  --compressed")
        return sb.toString()
    }

    fun generateCurl(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ): String {
        val sb = StringBuilder("curl")
        if (method.uppercase() != "GET") {
            sb.append(" -X ").append(method.uppercase())
        }
        sb.append(" '").append(url).append("'")

        for ((key, value) in headers) {
            if (key.isNotBlank()) {
                val escapedValue = value.replace("'", "'\\''")
                sb.append(" \\\n  -H '").append(key).append(": ").append(escapedValue).append("'")
            }
        }

        if (!body.isNullOrBlank() && method.uppercase() in listOf("POST", "PUT", "PATCH", "DELETE")) {
            val escapedBody = body.replace("'", "'\\''")
            sb.append(" \\\n  --data-raw '").append(escapedBody).append("'")
        }

        sb.append(" \\\n  --compressed")
        return sb.toString()
    }
}
