package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ReqInspectDatabase
import com.example.data.model.ConsoleLogItem
import com.example.data.model.InterceptedRequestEntity
import com.example.data.model.MockRuleEntity
import com.example.data.model.SavedSessionEntity
import com.example.data.repository.RequestRepository
import com.example.engine.CurlGenerator
import com.example.engine.DevToolsEventListener
import com.example.engine.HttpClientEngine
import com.example.engine.HttpResponseResult
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.UUID

enum class AppTab {
    BROWSER,
    INSPECTOR,
    COMPOSER,
    MOCK_RULES,
    MEDIA,
    CONSOLE,
    SESSIONS
}

data class HeaderEntry(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val value: String
)

class ReqInspectViewModel(application: Application) : AndroidViewModel(application), DevToolsEventListener {

    private val db = ReqInspectDatabase.getDatabase(application)
    val repository = RequestRepository(db.requestDao(), db.mockRuleDao(), db.sessionDao())
    private val httpClient = HttpClientEngine()

    // --- State: Language & Navigation ---
    private val _currentLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.BROWSER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // --- State: Browser & Recording ---
    val currentUrl = MutableStateFlow("https://httpbin.org/get")
    val isRecording = MutableStateFlow(true)
    val isDesktopMode = MutableStateFlow(false)
    val isPageLoading = MutableStateFlow(false)
    val pageProgress = MutableStateFlow(0f)
    val canGoBack = MutableStateFlow(false)
    val canGoForward = MutableStateFlow(false)

    // JS execution trigger
    val jsExecutionCode = MutableSharedFlow<String>(extraBufferCapacity = 5)

    // --- State: Network Requests & Filtering ---
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("ALL") // ALL, XHR_FETCH, DOC, MEDIA, JS, CSS, IMG, ERRORS
    val filterMethod = MutableStateFlow("ALL")

    val allRequests = repository.allRequests.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val activeRules = repository.allRules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val savedSessions = repository.allSessions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val filteredRequests: StateFlow<List<InterceptedRequestEntity>> = combine(
        allRequests,
        searchQuery,
        filterType,
        filterMethod
    ) { requests, query, type, method ->
        requests.filter { req ->
            val matchesQuery = if (query.isBlank()) true else {
                req.url.contains(query, ignoreCase = true) ||
                req.host.contains(query, ignoreCase = true) ||
                req.requestHeadersJson.contains(query, ignoreCase = true) ||
                (req.requestBody?.contains(query, ignoreCase = true) == true) ||
                (req.responseBody?.contains(query, ignoreCase = true) == true)
            }

            val matchesMethod = if (method == "ALL") true else req.method.equals(method, ignoreCase = true)

            val matchesType = when (type) {
                "ALL" -> true
                "XHR_FETCH" -> req.resourceType.lowercase() in listOf("xhr", "fetch")
                "DOC" -> req.resourceType.lowercase() in listOf("document", "doc")
                "MEDIA" -> req.resourceType.lowercase() in listOf("media", "video", "audio") ||
                           req.url.contains(Regex("\\.(mp4|m3u8|mp3|webm|aac|ts)(\\?|$)", RegexOption.IGNORE_CASE))
                "JS" -> req.resourceType.lowercase() in listOf("script", "js") || req.url.endsWith(".js")
                "CSS" -> req.resourceType.lowercase() in listOf("stylesheet", "css") || req.url.endsWith(".css")
                "IMG" -> req.resourceType.lowercase() in listOf("image", "img") ||
                         req.url.contains(Regex("\\.(png|jpg|jpeg|gif|webp|svg|ico)(\\?|$)", RegexOption.IGNORE_CASE))
                "ERRORS" -> req.statusCode >= 400 || req.statusCode == 0
                else -> true
            }

            matchesQuery && matchesMethod && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected request for details modal
    val selectedRequest = MutableStateFlow<InterceptedRequestEntity?>(null)

    // --- State: Media Sniffer ---
    val mediaRequests: StateFlow<List<InterceptedRequestEntity>> = allRequests.map { list ->
        list.filter { req ->
            req.resourceType.lowercase() in listOf("media", "video", "audio") ||
            req.url.contains(Regex("\\.(mp4|m3u8|mp3|webm|aac|m4a|ts|wav)(\\?|$)", RegexOption.IGNORE_CASE)) ||
            (req.contentType?.contains("video") == true || req.contentType?.contains("audio") == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State: Console Logs ---
    private val _consoleLogs = MutableStateFlow<List<ConsoleLogItem>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogItem>> = _consoleLogs.asStateFlow()

    // --- State: Composer ---
    val composerUrl = MutableStateFlow("https://httpbin.org/post")
    val composerMethod = MutableStateFlow("POST")
    val composerHeaders = MutableStateFlow(
        listOf(
            HeaderEntry(key = "Content-Type", value = "application/json"),
            HeaderEntry(key = "User-Agent", value = "ReqInspect/2.0 Android"),
            HeaderEntry(key = "Accept", value = "application/json")
        )
    )
    val composerBody = MutableStateFlow("{\n  \"name\": \"ReqInspect\",\n  \"status\": \"active\",\n  \"devtools\": true\n}")
    val composerLoading = MutableStateFlow(false)
    val composerResponse = MutableStateFlow<HttpResponseResult?>(null)
    val composerError = MutableStateFlow<String?>(null)

    // Toast / Banner feedback message
    val toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 5)

    // --- Actions ---

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleRecording() {
        isRecording.value = !isRecording.value
    }

    fun toggleDesktopMode() {
        isDesktopMode.value = !isDesktopMode.value
    }

    fun clearAllRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllRequests()
        }
    }

    fun toggleBookmark(request: InterceptedRequestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(request.id, !request.isBookmarked)
        }
    }

    fun deleteRequest(request: InterceptedRequestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRequest(request.id)
            if (selectedRequest.value?.id == request.id) {
                selectedRequest.value = null
            }
        }
    }

    fun selectRequest(request: InterceptedRequestEntity?) {
        selectedRequest.value = request
    }

    // DevTools Listener Callbacks
    override fun onNetworkRequestCaptured(
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
    ) {
        if (!isRecording.value) return

        viewModelScope.launch(Dispatchers.IO) {
            // Check active mock rules
            val rules = repository.getActiveRulesSync()
            var matchedRule: MockRuleEntity? = null
            for (rule in rules) {
                if (rule.matches(url)) {
                    matchedRule = rule
                    break
                }
            }

            val isBlocked = matchedRule?.actionType == "block"
            val isMocked = matchedRule?.actionType == "mock_response"

            val effectiveStatus = if (isMocked) matchedRule?.mockStatusCode ?: 200 else statusCode
            val effectiveResponse = if (isMocked) matchedRule?.mockResponseBody ?: "" else responseBody

            val entity = InterceptedRequestEntity(
                url = url,
                method = method,
                statusCode = effectiveStatus,
                statusText = if (isBlocked) "Blocked by Rule" else statusText,
                resourceType = resourceType,
                requestHeadersJson = requestHeadersJson,
                responseHeadersJson = if (isMocked) matchedRule?.mockHeadersJson ?: "{}" else responseHeadersJson,
                requestBody = requestBody,
                responseBody = effectiveResponse,
                contentLength = contentLength,
                durationMs = durationMs,
                isBlocked = isBlocked,
                isMocked = isMocked
            )
            repository.insertRequest(entity)
        }
    }

    override fun onConsoleLogReceived(level: String, message: String) {
        val item = ConsoleLogItem(level = level, message = message)
        _consoleLogs.value = (listOf(item) + _consoleLogs.value).take(200)
    }

    fun executeJsInBrowser(script: String) {
        if (script.isNotBlank()) {
            jsExecutionCode.tryEmit(script)
            onConsoleLogReceived("info", "▶ $script")
        }
    }

    fun clearConsoleLogs() {
        _consoleLogs.value = emptyList()
    }

    // Composer Actions
    fun sendComposerRequest() {
        val url = composerUrl.value.trim()
        if (url.isBlank()) return

        composerLoading.value = true
        composerError.value = null
        composerResponse.value = null

        val headersMap = composerHeaders.value
            .filter { it.key.isNotBlank() }
            .associate { it.key to it.value }

        viewModelScope.launch {
            val result = httpClient.executeRequest(
                url = url,
                method = composerMethod.value,
                headers = headersMap,
                body = composerBody.value
            )

            composerLoading.value = false
            result.onSuccess { res ->
                composerResponse.value = res
                // Also capture this composer request in logs
                onNetworkRequestCaptured(
                    url = url,
                    method = composerMethod.value,
                    statusCode = res.statusCode,
                    statusText = res.statusMessage,
                    resourceType = "fetch",
                    requestHeadersJson = JSONObject(headersMap).toString(),
                    responseHeadersJson = JSONObject(res.headers).toString(),
                    requestBody = composerBody.value,
                    responseBody = res.body,
                    contentLength = res.contentLength,
                    durationMs = res.durationMs
                )
            }.onFailure { err ->
                composerError.value = err.localizedMessage ?: "Request execution failed"
            }
        }
    }

    fun populateComposerFromRequest(request: InterceptedRequestEntity) {
        composerUrl.value = request.url
        composerMethod.value = request.method
        val headers = request.getRequestHeadersMap()
        composerHeaders.value = if (headers.isEmpty()) {
            listOf(HeaderEntry(key = "Content-Type", value = "application/json"))
        } else {
            headers.map { HeaderEntry(key = it.key, value = it.value) }
        }
        composerBody.value = request.requestBody ?: ""
        _currentTab.value = AppTab.COMPOSER
    }

    fun formatComposerJson() {
        try {
            val raw = composerBody.value
            if (raw.trim().startsWith("{")) {
                val obj = JSONObject(raw)
                composerBody.value = obj.toString(2)
            } else if (raw.trim().startsWith("[")) {
                val arr = JSONArray(raw)
                composerBody.value = arr.toString(2)
            }
        } catch (_: Exception) {}
    }

    fun addComposerHeader(key: String = "", value: String = "") {
        composerHeaders.value = composerHeaders.value + HeaderEntry(key = key, value = value)
    }

    fun updateComposerHeader(id: String, key: String, value: String) {
        composerHeaders.value = composerHeaders.value.map {
            if (it.id == id) it.copy(key = key, value = value) else it
        }
    }

    fun removeComposerHeader(id: String) {
        composerHeaders.value = composerHeaders.value.filter { it.id != id }
    }

    // Mock Rules Actions
    fun saveRule(rule: MockRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (rule.id == 0L) {
                repository.insertRule(rule)
            } else {
                repository.updateRule(rule)
            }
        }
    }

    fun toggleRule(rule: MockRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleRule(rule.id, !rule.isEnabled)
        }
    }

    fun deleteRule(rule: MockRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRule(rule.id)
        }
    }

    fun createRuleFromRequest(request: InterceptedRequestEntity) {
        val rule = MockRuleEntity(
            name = "Mock " + request.path.take(25),
            urlPattern = request.url,
            matchType = "contains",
            isEnabled = true,
            actionType = "mock_response",
            mockStatusCode = if (request.statusCode > 0) request.statusCode else 200,
            mockResponseBody = request.responseBody ?: "{\"mocked\": true}",
            mockHeadersJson = "{\"Content-Type\": \"application/json\", \"X-ReqInspect-Mock\": \"true\"}"
        )
        saveRule(rule)
        _currentTab.value = AppTab.MOCK_RULES
    }

    // Sessions & HAR Export
    fun saveCurrentSession(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val requests = allRequests.value
            val domains = requests.map { it.host }.distinct().take(4).joinToString(", ")
            val session = SavedSessionEntity(
                id = UUID.randomUUID().toString(),
                title = if (title.isBlank()) "Session ${System.currentTimeMillis()}" else title,
                createdAt = System.currentTimeMillis(),
                requestCount = requests.size,
                totalBytes = requests.sumOf { it.contentLength },
                domainsSummary = domains
            )
            repository.saveSession(session)
            toastMessage.tryEmit("Saved session: ${session.title}")
        }
    }

    fun deleteSession(session: SavedSessionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSession(session.id)
        }
    }

    fun exportHarJson(): String {
        val root = JSONObject()
        val log = JSONObject()
        log.put("version", "1.2")
        
        val creator = JSONObject()
        creator.put("name", "ReqInspect DevTools")
        creator.put("version", "2.0")
        log.put("creator", creator)

        val entries = JSONArray()
        allRequests.value.forEach { req ->
            val entry = JSONObject()
            entry.put("startedDateTime", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date(req.timestamp)))
            entry.put("time", req.durationMs)

            val reqObj = JSONObject()
            reqObj.put("method", req.method)
            reqObj.put("url", req.url)
            reqObj.put("httpVersion", "HTTP/1.1")
            
            val reqHeaders = JSONArray()
            req.getRequestHeadersMap().forEach { (k, v) ->
                val h = JSONObject()
                h.put("name", k)
                h.put("value", v)
                reqHeaders.put(h)
            }
            reqObj.put("headers", reqHeaders)
            if (!req.requestBody.isNullOrBlank()) {
                val postData = JSONObject()
                postData.put("mimeType", req.contentType ?: "text/plain")
                postData.put("text", req.requestBody)
                reqObj.put("postData", postData)
            }
            entry.put("request", reqObj)

            val resObj = JSONObject()
            resObj.put("status", req.statusCode)
            resObj.put("statusText", req.statusText)
            resObj.put("httpVersion", "HTTP/1.1")
            
            val resHeaders = JSONArray()
            req.getResponseHeadersMap().forEach { (k, v) ->
                val h = JSONObject()
                h.put("name", k)
                h.put("value", v)
                resHeaders.put(h)
            }
            resObj.put("headers", resHeaders)
            
            val content = JSONObject()
            content.put("size", req.contentLength)
            content.put("mimeType", req.contentType ?: "text/plain")
            content.put("text", req.responseBody ?: "")
            resObj.put("content", content)

            entry.put("response", resObj)
            entries.put(entry)
        }

        log.put("entries", entries)
        root.put("log", log)
        return root.toString(2)
    }

    fun copyToClipboard(text: String, label: String = "ReqInspect") {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        toastMessage.tryEmit("Copied to clipboard!")
    }
}
