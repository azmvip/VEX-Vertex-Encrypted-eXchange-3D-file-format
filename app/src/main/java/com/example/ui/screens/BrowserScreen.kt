package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.DevToolsBridge
import com.example.engine.DevToolsScript
import com.example.ui.components.PulsingRecordingIndicator
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.ReqInspectViewModel
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: ReqInspectViewModel,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isDesktopMode by viewModel.isDesktopMode.collectAsStateWithLifecycle()
    val isPageLoading by viewModel.isPageLoading.collectAsStateWithLifecycle()
    val pageProgress by viewModel.pageProgress.collectAsStateWithLifecycle()
    val canGoBack by viewModel.canGoBack.collectAsStateWithLifecycle()
    val canGoForward by viewModel.canGoForward.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()

    var urlInput by remember { mutableStateOf(currentUrl) }
    val focusManager = LocalFocusManager.current

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showQuickJsBar by remember { mutableStateOf(false) }
    var jsInputText by remember { mutableStateOf("") }

    // Preset test websites
    val quickSites = listOf(
        "httpbin.org/get" to "https://httpbin.org/get",
        "reqres.in (API)" to "https://reqres.in",
        "jsonplaceholder" to "https://jsonplaceholder.typicode.com",
        "HackerNews" to "https://news.ycombinator.com",
        "Wikipedia" to "https://en.m.wikipedia.org"
    )

    // Listen to JS execution requests from VM
    LaunchedEffect(webViewRef) {
        viewModel.jsExecutionCode.collectLatest { script ->
            webViewRef?.evaluateJavascript(script) { result ->
                if (result != null && result != "null") {
                    viewModel.onConsoleLogReceived("info", "◀ $result")
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // --- 1. Top URL Bar & DevTools Controls ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Navigation controls
                IconButton(
                    onClick = { webViewRef?.goBack() },
                    enabled = canGoBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (canGoBack) CyberCyan else CyberTextMuted
                    )
                }

                IconButton(
                    onClick = { webViewRef?.goForward() },
                    enabled = canGoForward,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (canGoForward) CyberCyan else CyberTextMuted
                    )
                }

                // URL Input Field
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = CyberTextPrimary,
                        fontFamily = CodeFont
                    ),
                    placeholder = {
                        Text(
                            text = AppStrings.enterUrl(lang),
                            color = CyberTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (urlInput.startsWith("https")) Icons.Default.Lock else Icons.Default.Public,
                            contentDescription = "Security",
                            tint = if (urlInput.startsWith("https")) CyberGreen else CyberAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (isPageLoading) {
                            IconButton(onClick = { webViewRef?.stopLoading() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Stop",
                                    tint = CyberRose,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    var target = urlInput.trim()
                                    if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                        target = "https://$target"
                                        urlInput = target
                                    }
                                    focusManager.clearFocus()
                                    viewModel.currentUrl.value = target
                                    webViewRef?.loadUrl(target)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Go",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            var target = urlInput.trim()
                            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                target = "https://$target"
                                urlInput = target
                            }
                            focusManager.clearFocus()
                            viewModel.currentUrl.value = target
                            webViewRef?.loadUrl(target)
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    )
                )

                // Refresh Button
                IconButton(
                    onClick = { webViewRef?.reload() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = CyberCyan
                    )
                }
            }

            // Quick Sites & Toggles Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Record toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.toggleRecording() }
                ) {
                    PulsingRecordingIndicator(isRecording = isRecording)
                }

                // Desktop / Mobile Mode Toggle
                AssistChip(
                    onClick = {
                        viewModel.toggleDesktopMode()
                        webViewRef?.settings?.userAgentString = if (!isDesktopMode) {
                            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 ReqInspect/2.0"
                        } else null
                        webViewRef?.reload()
                    },
                    label = {
                        Text(
                            text = if (isDesktopMode) "🖥️ Desktop" else "📱 Mobile",
                            fontSize = 11.sp,
                            color = if (isDesktopMode) CyberCyan else CyberTextSecondary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isDesktopMode) CyberCyan.copy(alpha = 0.15f) else CyberSurfaceVariant
                    ),
                    border = null
                )

                // Quick JS Console Toggle
                AssistChip(
                    onClick = { showQuickJsBar = !showQuickJsBar },
                    label = {
                        Text(
                            text = "⚡ JS Console",
                            fontSize = 11.sp,
                            color = if (showQuickJsBar) CyberAmber else CyberTextSecondary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (showQuickJsBar) CyberAmber.copy(alpha = 0.15f) else CyberSurfaceVariant
                    ),
                    border = null
                )

                // Quick Test Sites
                quickSites.forEach { (title, link) ->
                    SuggestionChip(
                        onClick = {
                            urlInput = link
                            viewModel.currentUrl.value = link
                            webViewRef?.loadUrl(link)
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                color = CyberTextSecondary
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = CyberSurfaceVariant
                        ),
                        border = null
                    )
                }
            }

            // Quick JS Evaluator Bar
            AnimatedVisibility(visible = showQuickJsBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = jsInputText,
                        onValueChange = { jsInputText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = {
                            Text("e.g. document.title, window.location.href", fontSize = 12.sp, color = CyberTextMuted)
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = CyberAmber,
                            fontFamily = CodeFont
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberAmber,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant
                        )
                    )

                    Button(
                        onClick = {
                            if (jsInputText.isNotBlank()) {
                                viewModel.executeJsInBrowser(jsInputText)
                                jsInputText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Run", color = Color(0xFF1E1B0A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 2. Loading Progress Bar ---
        if (isPageLoading && pageProgress < 1f) {
            LinearProgressIndicator(
                progress = pageProgress,
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = CyberCyan,
                trackColor = CyberSurfaceVariant
            )
        }

        // --- 3. Embedded Intercepting WebView with Floating DevTools HUD ---
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }

                        // Add JS Bridge
                        addJavascriptInterface(DevToolsBridge(viewModel), "ReqInspectBridge")

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let {
                                    urlInput = it
                                    viewModel.currentUrl.value = it
                                }
                                viewModel.isPageLoading.value = true
                                viewModel.canGoBack.value = view?.canGoBack() == true
                                viewModel.canGoForward.value = view?.canGoForward() == true
                                // Inject DevTools hook script
                                view?.evaluateJavascript(DevToolsScript.INJECTION_AGENT_JS, null)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                viewModel.isPageLoading.value = false
                                viewModel.canGoBack.value = view?.canGoBack() == true
                                viewModel.canGoForward.value = view?.canGoForward() == true
                                // Ensure DevTools hook is injected
                                view?.evaluateJavascript(DevToolsScript.INJECTION_AGENT_JS, null)
                            }

                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                // Allow SSL inspection during debugging
                                handler?.proceed()
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val reqUrl = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

                                // Inject agent into document frames
                                if (request.isForMainFrame) {
                                    // Let normal loader handle, JS will hook inside
                                    return super.shouldInterceptRequest(view, request)
                                }

                                // Check mock rules
                                val rules = viewModel.activeRules.value
                                for (rule in rules) {
                                    if (rule.isEnabled && rule.matches(reqUrl)) {
                                        if (rule.actionType == "block") {
                                            val reqHeadersMap = request.requestHeaders ?: emptyMap<String, String>()
                                            viewModel.onNetworkRequestCaptured(
                                                url = reqUrl,
                                                method = request.method ?: "GET",
                                                statusCode = 403,
                                                statusText = "Blocked by Rule",
                                                resourceType = "blocked",
                                                requestHeadersJson = JSONObject(reqHeadersMap).toString(),
                                                responseHeadersJson = "{}",
                                                requestBody = null,
                                                responseBody = "Blocked by ReqInspect Rule",
                                                contentLength = 0,
                                                durationMs = 0
                                            )
                                            return WebResourceResponse(
                                                "text/plain",
                                                "UTF-8",
                                                403,
                                                "Blocked by Rule",
                                                mapOf<String, String>(),
                                                ByteArrayInputStream(ByteArray(0))
                                            )
                                        } else if (rule.actionType == "mock_response") {
                                            val mockBytes = rule.mockResponseBody.toByteArray()
                                            val reqHeadersMap = request.requestHeaders ?: emptyMap<String, String>()
                                            viewModel.onNetworkRequestCaptured(
                                                url = reqUrl,
                                                method = request.method ?: "GET",
                                                statusCode = rule.mockStatusCode,
                                                statusText = "Mocked Response",
                                                resourceType = "mock",
                                                requestHeadersJson = JSONObject(reqHeadersMap).toString(),
                                                responseHeadersJson = rule.mockHeadersJson,
                                                requestBody = null,
                                                responseBody = rule.mockResponseBody,
                                                contentLength = mockBytes.size.toLong(),
                                                durationMs = 5
                                            )
                                            return WebResourceResponse(
                                                "application/json",
                                                "UTF-8",
                                                rule.mockStatusCode,
                                                "OK",
                                                rule.getMockHeadersMap(),
                                                ByteArrayInputStream(mockBytes)
                                            )
                                        }
                                    }
                                }

                                // Fallback interceptor for non-XHR resources (Images, Media, Stylesheets, Fonts)
                                val method = request.method ?: "GET"
                                val type = when {
                                    reqUrl.contains(Regex("\\.(mp4|m3u8|mp3|webm|aac|ts)(\\?|$)", RegexOption.IGNORE_CASE)) -> "media"
                                    reqUrl.contains(Regex("\\.(png|jpg|jpeg|gif|webp|svg|ico)(\\?|$)", RegexOption.IGNORE_CASE)) -> "image"
                                    reqUrl.contains(Regex("\\.(css)(\\?|$)", RegexOption.IGNORE_CASE)) -> "stylesheet"
                                    reqUrl.contains(Regex("\\.(js)(\\?|$)", RegexOption.IGNORE_CASE)) -> "script"
                                    reqUrl.contains(Regex("\\.(woff|woff2|ttf|eot)(\\?|$)", RegexOption.IGNORE_CASE)) -> "font"
                                    else -> "other"
                                }

                                if (type != "other" && viewModel.isRecording.value) {
                                    val reqHeadersMap = request.requestHeaders ?: emptyMap<String, String>()
                                    viewModel.onNetworkRequestCaptured(
                                        url = reqUrl,
                                        method = method,
                                        statusCode = 200,
                                        statusText = "OK",
                                        resourceType = type,
                                        requestHeadersJson = JSONObject(reqHeadersMap).toString(),
                                        responseHeadersJson = "{}",
                                        requestBody = null,
                                        responseBody = null,
                                        contentLength = 0,
                                        durationMs = 15
                                    )
                                }

                                return super.shouldInterceptRequest(view, request)
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                viewModel.pageProgress.value = newProgress / 100f
                                if (newProgress > 30) {
                                    view?.evaluateJavascript(DevToolsScript.INJECTION_AGENT_JS, null)
                                }
                            }

                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    val level = when (it.messageLevel()) {
                                        ConsoleMessage.MessageLevel.ERROR -> "error"
                                        ConsoleMessage.MessageLevel.WARNING -> "warn"
                                        ConsoleMessage.MessageLevel.LOG -> "log"
                                        else -> "info"
                                    }
                                    viewModel.onConsoleLogReceived(level, "${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                                }
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }

                        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                            viewModel.onNetworkRequestCaptured(
                                url = url,
                                method = "GET",
                                statusCode = 200,
                                statusText = "Download Detected",
                                resourceType = "media",
                                requestHeadersJson = "{\"User-Agent\": \"$userAgent\"}",
                                responseHeadersJson = "{\"Content-Disposition\": \"$contentDisposition\", \"Content-Type\": \"$mimetype\"}",
                                requestBody = null,
                                responseBody = "Downloadable File: $mimetype ($contentLength bytes)",
                                contentLength = contentLength,
                                durationMs = 0
                            )
                        }

                        loadUrl(currentUrl)
                        webViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // --- Floating DevTools HUD Badge (Reqable / Kiwi style) ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CyberSurface.copy(alpha = 0.92f))
                    .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .clickable { viewModel.setTab(AppTab.INSPECTOR) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) CyberRose else CyberAmber)
                    )
                    Text(
                        text = "${allRequests.size} reqs",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CodeFont
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Inspect",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
