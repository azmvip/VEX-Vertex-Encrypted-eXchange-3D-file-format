package com.example.engine

import android.webkit.JavascriptInterface
import org.json.JSONObject

interface DevToolsEventListener {
    fun onNetworkRequestCaptured(
        url: String,
        method: String,
        statusCode: Int,
        statusText: String,
        resourceType: String,
        requestHeadersJson: String,
        responseHeadersJson: String,
        requestBody: String?,
        responseBody: String?,
        contentLength: Long,
        durationMs: Long
    )

    fun onConsoleLogReceived(level: String, message: String)
}

class DevToolsBridge(private val listener: DevToolsEventListener) {

    @JavascriptInterface
    fun onFetchEvent(
        url: String?,
        method: String?,
        statusCode: Int,
        statusText: String?,
        resourceType: String?,
        requestHeadersJson: String?,
        responseHeadersJson: String?,
        requestBody: String?,
        responseBody: String?,
        contentLength: Long,
        durationMs: Long
    ) {
        if (url.isNullOrBlank()) return
        listener.onNetworkRequestCaptured(
            url = url,
            method = method ?: "GET",
            statusCode = statusCode,
            statusText = statusText ?: "OK",
            resourceType = resourceType ?: "fetch",
            requestHeadersJson = requestHeadersJson ?: "{}",
            responseHeadersJson = responseHeadersJson ?: "{}",
            requestBody = requestBody,
            responseBody = responseBody,
            contentLength = contentLength,
            durationMs = durationMs
        )
    }

    @JavascriptInterface
    fun onXhrEvent(
        url: String?,
        method: String?,
        statusCode: Int,
        statusText: String?,
        resourceType: String?,
        requestHeadersJson: String?,
        responseHeadersJson: String?,
        requestBody: String?,
        responseBody: String?,
        contentLength: Long,
        durationMs: Long
    ) {
        if (url.isNullOrBlank()) return
        listener.onNetworkRequestCaptured(
            url = url,
            method = method ?: "GET",
            statusCode = statusCode,
            statusText = statusText ?: "OK",
            resourceType = resourceType ?: "xhr",
            requestHeadersJson = requestHeadersJson ?: "{}",
            responseHeadersJson = responseHeadersJson ?: "{}",
            requestBody = requestBody,
            responseBody = responseBody,
            contentLength = contentLength,
            durationMs = durationMs
        )
    }

    @JavascriptInterface
    fun onConsoleLog(level: String?, message: String?) {
        if (message.isNullOrBlank()) return
        listener.onConsoleLogReceived(
            level = level ?: "log",
            message = message
        )
    }
}
