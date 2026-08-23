package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class HttpResponseResult(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String>,
    val body: String,
    val durationMs: Long,
    val contentLength: Long,
    val contentType: String?
)

class HttpClientEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun executeRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ): Result<HttpResponseResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val reqBuilder = Request.Builder().url(url)

            // Add headers
            headers.forEach { (key, value) ->
                if (key.isNotBlank()) {
                    reqBuilder.addHeader(key, value)
                }
            }

            // Body
            val upperMethod = method.uppercase()
            val mediaType = (headers["Content-Type"] ?: headers["content-type"] ?: "application/json; charset=utf-8").toMediaTypeOrNull()
            
            val requestBody = when {
                upperMethod in listOf("POST", "PUT", "PATCH") -> {
                    (body ?: "").toRequestBody(mediaType)
                }
                upperMethod == "DELETE" && !body.isNullOrBlank() -> {
                    body.toRequestBody(mediaType)
                }
                else -> null
            }

            reqBuilder.method(upperMethod, requestBody)

            val call = client.newCall(reqBuilder.build())
            val response = call.execute()
            val duration = System.currentTimeMillis() - startTime

            val resHeaders = mutableMapOf<String, String>()
            for (i in 0 until response.headers.size) {
                resHeaders[response.headers.name(i)] = response.headers.value(i)
            }

            val responseBodyString = response.body?.string() ?: ""
            val contentType = response.header("Content-Type")

            Result.success(
                HttpResponseResult(
                    statusCode = response.code,
                    statusMessage = response.message,
                    headers = resHeaders,
                    body = responseBodyString,
                    durationMs = duration,
                    contentLength = responseBodyString.length.toLong(),
                    contentType = contentType
                )
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Result.failure(e)
        }
    }
}
