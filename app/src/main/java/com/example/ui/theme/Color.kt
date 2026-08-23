package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Cyber / Reqable DevTools Theme Palette
val CyberBg = Color(0xFF0B0F19)
val CyberSurface = Color(0xFF131B2E)
val CyberSurfaceVariant = Color(0xFF1B243B)
val CyberCardBorder = Color(0xFF24304D)

val CyberCyan = Color(0xFF00F0FF)
val CyberCyanDark = Color(0xFF0099AA)
val CyberViolet = Color(0xFF8B5CF6)
val CyberGreen = Color(0xFF00FF9D)
val CyberGreenDark = Color(0xFF00A865)
val CyberAmber = Color(0xFFFFB800)
val CyberRose = Color(0xFFFF3366)
val CyberBlue = Color(0xFF3B82F6)

val CyberTextPrimary = Color(0xFFF8FAFC)
val CyberTextSecondary = Color(0xFF94A3B8)
val CyberTextMuted = Color(0xFF64748B)

// Status Code Color mapping
fun getStatusColor(code: Int): Color {
    return when {
        code in 200..299 -> CyberGreen
        code in 300..399 -> CyberCyan
        code in 400..499 -> CyberAmber
        code >= 500 -> CyberRose
        code == 0 -> Color(0xFFE11D48)
        else -> CyberTextSecondary
    }
}

// Method Color mapping
fun getMethodColor(method: String): Color {
    return when (method.uppercase()) {
        "GET" -> CyberGreen
        "POST" -> CyberCyan
        "PUT" -> CyberAmber
        "PATCH" -> Color(0xFFF59E0B)
        "DELETE" -> CyberRose
        "OPTIONS", "HEAD" -> CyberViolet
        "WS", "WEBSOCKET" -> Color(0xFFEC4899)
        else -> CyberBlue
    }
}

// Resource Type Tag Color
fun getResourceTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "fetch", "xhr" -> CyberCyan
        "document", "doc" -> CyberBlue
        "script", "js" -> CyberAmber
        "stylesheet", "css" -> CyberViolet
        "image", "img" -> Color(0xFF10B981)
        "media", "video", "audio" -> Color(0xFFF43F5E)
        "font" -> Color(0xFFA855F7)
        "websocket", "ws" -> Color(0xFFEC4899)
        else -> CyberTextMuted
    }
}
