package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReqInspectViewModel

@Composable
fun ConsoleScreen(
    viewModel: ReqInspectViewModel,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.consoleLogs.collectAsStateWithLifecycle()
    var jsInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(12.dp)
    ) {
        CyberSectionHeader(
            title = AppStrings.consoleTitle(lang),
            icon = Icons.Default.Terminal,
            action = {
                TextButton(
                    onClick = { viewModel.clearConsoleLogs() },
                    colors = ButtonDefaults.textButtonColors(contentColor = CyberRose)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.clearConsole(lang), fontSize = 11.sp)
                }
            }
        )

        // Console Logs Feed
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF070B12))
                .padding(8.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No JavaScript console messages captured yet.\nAny console.log / info / warn / error from web pages will stream live here.",
                        color = CyberTextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val color = when (log.level.lowercase()) {
                            "error" -> CyberRose
                            "warn" -> CyberAmber
                            "info" -> CyberCyan
                            else -> CyberTextPrimary
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "[${log.level.uppercase()}]",
                                color = color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CodeFont
                            )
                            Text(
                                text = log.message,
                                color = color,
                                fontSize = 11.sp,
                                fontFamily = CodeFont,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive JS input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = jsInput,
                onValueChange = { jsInput = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(AppStrings.evalJs(lang), fontSize = 12.sp, color = CyberTextMuted) },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = CyberAmber, fontFamily = CodeFont),
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
                    if (jsInput.isNotBlank()) {
                        viewModel.executeJsInBrowser(jsInput)
                        jsInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(AppStrings.execute(lang), color = Color(0xFF1E1B0A), fontWeight = FontWeight.Bold)
            }
        }
    }
}
