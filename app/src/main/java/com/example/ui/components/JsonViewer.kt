package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun JsonViewer(
    jsonString: String?,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No Body / Payload"
) {
    if (jsonString.isNullOrBlank()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberSurfaceVariant.copy(alpha = 0.4f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                color = CyberTextMuted,
                fontSize = 13.sp,
                fontFamily = CodeFont
            )
        }
        return
    }

    val formattedJson = remember(jsonString) {
        try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("{")) {
                JSONObject(trimmed).toString(2)
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString(2)
            } else {
                jsonString
            }
        } catch (_: Exception) {
            jsonString
        }
    }

    val annotatedCode = remember(formattedJson) {
        buildHighlightedJson(formattedJson)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090D16))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
    ) {
        // Toolbar header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(CyberRose))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(CyberAmber))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(CyberGreen))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${formattedJson.length} bytes",
                    color = CyberTextMuted,
                    fontSize = 11.sp,
                    fontFamily = CodeFont
                )
            }

            IconButton(
                onClick = { onCopy(formattedJson) },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy JSON",
                    tint = CyberCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Code Content
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = annotatedCode,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontFamily = CodeFont
                )
            }
        }
    }
}

private fun buildHighlightedJson(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            var i = 0
            while (i < line.length) {
                val c = line[i]
                when {
                    c == '"' -> {
                        val endQuote = line.indexOf('"', i + 1)
                        if (endQuote != -1) {
                            val str = line.substring(i, endQuote + 1)
                            val isKey = line.substring(endQuote + 1).trimStart().startsWith(":")
                            if (isKey) {
                                pushStyle(SpanStyle(color = CyberCyan, fontWeight = FontWeight.SemiBold))
                            } else {
                                pushStyle(SpanStyle(color = CyberGreen))
                            }
                            append(str)
                            pop()
                            i = endQuote + 1
                        } else {
                            append(c)
                            i++
                        }
                    }
                    c in listOf('{', '}', '[', ']', ':', ',') -> {
                        pushStyle(SpanStyle(color = CyberTextSecondary))
                        append(c)
                        pop()
                        i++
                    }
                    c.isDigit() || c == '-' -> {
                        val numStart = i
                        while (i < line.length && (line[i].isDigit() || line[i] == '.' || line[i] == '-' || line[i] == 'e' || line[i] == 'E')) {
                            i++
                        }
                        val numStr = line.substring(numStart, i)
                        pushStyle(SpanStyle(color = CyberAmber))
                        append(numStr)
                        pop()
                    }
                    line.startsWith("true", i) -> {
                        pushStyle(SpanStyle(color = CyberViolet, fontWeight = FontWeight.Bold))
                        append("true")
                        pop()
                        i += 4
                    }
                    line.startsWith("false", i) -> {
                        pushStyle(SpanStyle(color = CyberRose, fontWeight = FontWeight.Bold))
                        append("false")
                        pop()
                        i += 5
                    }
                    line.startsWith("null", i) -> {
                        pushStyle(SpanStyle(color = CyberTextMuted, fontWeight = FontWeight.Bold))
                        append("null")
                        pop()
                        i += 4
                    }
                    else -> {
                        append(c)
                        i++
                    }
                }
            }
            if (index < lines.size - 1) append("\n")
        }
    }
}
