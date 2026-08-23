package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
fun SessionsScreen(
    viewModel: ReqInspectViewModel,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.savedSessions.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    var sessionTitleInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Language Switcher Card ---
        CyberCard {
            CyberSectionHeader(
                title = AppStrings.language(lang),
                icon = Icons.Default.Translate
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Arabic button
                Button(
                    onClick = { viewModel.setLanguage(AppLanguage.ARABIC) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lang == AppLanguage.ARABIC) CyberCyan else CyberSurfaceVariant,
                        contentColor = if (lang == AppLanguage.ARABIC) Color(0xFF00363D) else CyberTextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("العربية (Arabic)", fontWeight = FontWeight.Bold)
                }

                // English button
                Button(
                    onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lang == AppLanguage.ENGLISH) CyberCyan else CyberSurfaceVariant,
                        contentColor = if (lang == AppLanguage.ENGLISH) Color(0xFF00363D) else CyberTextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("English (الإنجليزية)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 2. HAR / JSON Export & Save Session ---
        CyberCard {
            CyberSectionHeader(
                title = AppStrings.exportHar(lang),
                icon = Icons.Default.FileDownload
            )

            Text(
                text = "Export the current session as a standard HAR (HTTP Archive) or JSON format to inspect in desktop DevTools, Reqable, Charles, or Postman.",
                color = CyberTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val harJson = viewModel.exportHarJson()
                        viewModel.copyToClipboard(harJson, "HAR JSON")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF003820), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy HAR JSON", color = Color(0xFF003820), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Session
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = sessionTitleInput,
                    onValueChange = { sessionTitleInput = it },
                    placeholder = { Text("Session Name (e.g. Login Flow)", fontSize = 12.sp, color = CyberTextMuted) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder
                    )
                )

                Button(
                    onClick = {
                        viewModel.saveCurrentSession(sessionTitleInput)
                        sessionTitleInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(AppStrings.save(lang), color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 3. Saved Sessions List ---
        CyberCard {
            CyberSectionHeader(
                title = AppStrings.tabSessions(lang),
                icon = Icons.Default.Bookmark
            )

            if (sessions.isEmpty()) {
                Text(
                    text = "No saved sessions yet. Save captured sessions above to review anytime.",
                    color = CyberTextMuted,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sessions.forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberSurfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = s.title, color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${s.requestCount} requests • ${s.domainsSummary}", color = CyberCyan, fontSize = 11.sp, fontFamily = CodeFont)
                            }
                            IconButton(onClick = { viewModel.deleteSession(s) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberRose, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- 4. DevTools Engine Info ---
        CyberCard {
            CyberSectionHeader(
                title = "ReqInspect DevTools Engine",
                icon = Icons.Default.Info
            )
            Text(
                text = "• Injected DevTools Agent: XMLHttpRequest & Fetch Monkey-patching Active\n• Resource Sniffer: CSS, Scripts, Documents, WebSockets & Media Streams\n• Mock Engine: Status Code & Custom Payload Spoofing\n• cURL Generator: 1-Tap command exporter for terminal replay\n• Dual Language: Full Arabic & English support",
                color = CyberTextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = CodeFont
            )
        }
    }
}
