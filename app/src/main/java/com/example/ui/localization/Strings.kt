package com.example.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ARABIC }

object AppStrings {

    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ReqInspect DevTools"
        AppLanguage.ENGLISH -> "ReqInspect DevTools"
    }

    // Tabs
    fun tabBrowser(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المتصفح"
        AppLanguage.ENGLISH -> "Browser"
    }

    fun tabInspector(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الشبكة"
        AppLanguage.ENGLISH -> "Network"
    }

    fun tabComposer(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المحرر"
        AppLanguage.ENGLISH -> "Composer"
    }

    fun tabRules(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "القواعد والمحاكاة"
        AppLanguage.ENGLISH -> "Mock Rules"
    }

    fun tabMedia(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "صياد الوسائط"
        AppLanguage.ENGLISH -> "Media Sniffer"
    }

    fun tabConsole(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "وحدة التحكم"
        AppLanguage.ENGLISH -> "Console"
    }

    fun tabSessions(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الجلسات"
        AppLanguage.ENGLISH -> "Sessions"
    }

    // Browser
    fun enterUrl(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أدخل رابط الموقع (URL)..."
        AppLanguage.ENGLISH -> "Enter website URL..."
    }

    fun recordingActive(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التسجيل نشط"
        AppLanguage.ENGLISH -> "Recording Live"
    }

    fun recordingPaused(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التسجيل متوقف"
        AppLanguage.ENGLISH -> "Recording Paused"
    }

    fun devToolsInjected(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أدوات DevTools مفعلة ومحقونة"
        AppLanguage.ENGLISH -> "DevTools Agent Active & Injected"
    }

    fun quickLinks(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مواقع سريعة للاختبار"
        AppLanguage.ENGLISH -> "Quick Test Sites"
    }

    fun desktopMode(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "وضع الكمبيوتر (Desktop)"
        AppLanguage.ENGLISH -> "Desktop Mode"
    }

    fun mobileMode(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "وضع الهاتف (Mobile)"
        AppLanguage.ENGLISH -> "Mobile Mode"
    }

    fun clearCache(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مسح الذاكرة المؤقتة"
        AppLanguage.ENGLISH -> "Clear Cache & Cookies"
    }

    // Network Inspector
    fun searchRequests(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "بحث في الطلبات (URL, Host, Headers)..."
        AppLanguage.ENGLISH -> "Search requests (URL, Host, Headers)..."
    }

    fun filterAll(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الكل"
        AppLanguage.ENGLISH -> "All"
    }

    fun filterXhrFetch(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "XHR / Fetch"
        AppLanguage.ENGLISH -> "XHR / Fetch"
    }

    fun filterDoc(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مستندات (Doc)"
        AppLanguage.ENGLISH -> "Doc"
    }

    fun filterMedia(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "وسائط / فيديو"
        AppLanguage.ENGLISH -> "Media / Video"
    }

    fun filterJs(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سكربت JS"
        AppLanguage.ENGLISH -> "JS"
    }

    fun filterCss(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أنماط CSS"
        AppLanguage.ENGLISH -> "CSS"
    }

    fun filterImg(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "صور"
        AppLanguage.ENGLISH -> "Images"
    }

    fun filterErrors(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أخطاء (4xx/5xx)"
        AppLanguage.ENGLISH -> "Errors (4xx/5xx)"
    }

    fun clearAllLogs(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مسح السجلات"
        AppLanguage.ENGLISH -> "Clear Logs"
    }

    fun noRequestsCaptured(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لم يتم اعتراض أي طلبات بعد. تصفح أي موقع في علامة التبويب الأولى لمشاهدة الحركة الحية!"
        AppLanguage.ENGLISH -> "No network requests intercepted yet. Browse any site in the browser tab to see live traffic!"
    }

    fun requestsCount(lang: AppLanguage, count: Int) = when (lang) {
        AppLanguage.ARABIC -> "$count طلب"
        AppLanguage.ENGLISH -> "$count requests"
    }

    // Request Details
    fun requestDetails(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفاصيل الطلب"
        AppLanguage.ENGLISH -> "Request Details"
    }

    fun tabGeneral(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "عام"
        AppLanguage.ENGLISH -> "General"
    }

    fun tabReqHeaders(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ترويسات الطلب"
        AppLanguage.ENGLISH -> "Request Headers"
    }

    fun tabResHeaders(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ترويسات الاستجابة"
        AppLanguage.ENGLISH -> "Response Headers"
    }

    fun tabPayload(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حمولة الطلب (Payload)"
        AppLanguage.ENGLISH -> "Request Payload"
    }

    fun tabResponse(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الاستجابة (Body)"
        AppLanguage.ENGLISH -> "Response Body"
    }

    fun tabQueryParams(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المعاملات (Params)"
        AppLanguage.ENGLISH -> "Query Params"
    }

    fun copyCurl(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نسخ كـ cURL"
        AppLanguage.ENGLISH -> "Copy as cURL"
    }

    fun copyUrl(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نسخ الرابط"
        AppLanguage.ENGLISH -> "Copy URL"
    }

    fun copyBody(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نسخ نص الاستجابة"
        AppLanguage.ENGLISH -> "Copy Response"
    }

    fun sendToComposer(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إعادة إرسال وتعديل (Composer)"
        AppLanguage.ENGLISH -> "Send to Composer"
    }

    fun createMockRule(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إنشاء قاعدة محاكاة"
        AppLanguage.ENGLISH -> "Create Mock Rule"
    }

    fun bookmarkRequest(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حفظ في المفضلة"
        AppLanguage.ENGLISH -> "Bookmark Request"
    }

    // Composer
    fun composerTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "محرر الطلبات ومختبر الـ API"
        AppLanguage.ENGLISH -> "API Request Composer & Tester"
    }

    fun sendRequest(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إرسال الطلب"
        AppLanguage.ENGLISH -> "Send Request"
    }

    fun addHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إضافة ترويسة"
        AppLanguage.ENGLISH -> "Add Header"
    }

    fun requestBodyHint(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أدخل نص الطلب (JSON / Raw)..."
        AppLanguage.ENGLISH -> "Enter Request Body (JSON / Raw)..."
    }

    fun formatJson(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تنسيق JSON"
        AppLanguage.ENGLISH -> "Format JSON"
    }

    // Mock Rules
    fun rulesTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "قواعد الاعتراض والمحاكاة (Mocking & Breakpoints)"
        AppLanguage.ENGLISH -> "Mocking & Breakpoint Rules"
    }

    fun addNewRule(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إضافة قاعدة جديدة"
        AppLanguage.ENGLISH -> "Add New Rule"
    }

    fun ruleName(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "اسم القاعدة"
        AppLanguage.ENGLISH -> "Rule Name"
    }

    fun urlPattern(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نمط الرابط (URL Pattern)"
        AppLanguage.ENGLISH -> "URL Pattern"
    }

    fun actionMockResponse(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "محاكاة استجابة مخصصة (Mock Response)"
        AppLanguage.ENGLISH -> "Mock Custom Response"
    }

    fun actionBlock(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حظر الطلب (Block)"
        AppLanguage.ENGLISH -> "Block Request"
    }

    fun statusCode(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "رمز الحالة (Status Code)"
        AppLanguage.ENGLISH -> "Status Code"
    }

    fun mockResponseBody(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نص الاستجابة المحاكاة (JSON / Text)"
        AppLanguage.ENGLISH -> "Mock Response Body (JSON / Text)"
    }

    fun save(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حفظ"
        AppLanguage.ENGLISH -> "Save"
    }

    fun cancel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إلغاء"
        AppLanguage.ENGLISH -> "Cancel"
    }

    fun delete(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حذف"
        AppLanguage.ENGLISH -> "Delete"
    }

    // Media Sniffer
    fun mediaTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "صياد الوسائط والروابط المستخرجة"
        AppLanguage.ENGLISH -> "Media & Stream Sniffer"
    }

    fun noMediaFound(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لم يتم رصد أي ملفات وسائط بعد (MP4, M3U8, MP3, WebM). تصفح المواقع التي تحتوي على فيديو أو صوت!"
        AppLanguage.ENGLISH -> "No media streams detected yet (MP4, M3U8, MP3, WebM). Browse media websites to sniff streams!"
    }

    // Console
    fun consoleTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سجل وحدة التحكم (Console Logs)"
        AppLanguage.ENGLISH -> "JavaScript Console Logs"
    }

    fun evalJs(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تنفيذ كود JavaScript في الصفحة..."
        AppLanguage.ENGLISH -> "Execute JavaScript on current page..."
    }

    fun execute(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تنفيذ"
        AppLanguage.ENGLISH -> "Execute"
    }

    fun clearConsole(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مسح الكونسول"
        AppLanguage.ENGLISH -> "Clear Console"
    }

    // Settings / Sessions
    fun saveSession(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حفظ الجلسة الحالية"
        AppLanguage.ENGLISH -> "Save Current Session"
    }

    fun exportHar(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تصدير بتنسيق HAR / JSON"
        AppLanguage.ENGLISH -> "Export as HAR / JSON"
    }

    fun language(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "اللغة (Language)"
        AppLanguage.ENGLISH -> "Language (اللغة)"
    }

    fun copiedToClipboard(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تم النسخ إلى الحافظة بنجاح!"
        AppLanguage.ENGLISH -> "Copied to clipboard!"
    }
}
