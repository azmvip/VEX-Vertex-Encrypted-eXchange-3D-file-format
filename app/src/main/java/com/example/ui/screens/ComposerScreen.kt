package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.CurlGenerator
import com.example.ui.components.*
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReqInspectViewModel

enum class ComposerSection {
    HEADERS,
    BODY,
    RESPONSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerScreen(
    viewModel: ReqInspectViewModel,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val url by viewModel.composerUrl.collectAsStateWithLifecycle()
    val method by viewModel.composerMethod.collectAsStateWithLifecycle()
    val headers by viewModel.composerHeaders.collectAsStateWithLifecycle()
    val body by viewModel.composerBody.collectAsStateWithLifecycle()
    val isLoading by viewModel.composerLoading.collectAsStateWithLifecycle()
    val response by viewModel.composerResponse.collectAsStateWithLifecycle()
    val error by viewModel.composerError.collectAsStateWithLifecycle()

    var activeSection by remember { mutableStateOf(ComposerSection.HEADERS) }
    var showMethodDropdown by remember { mutableStateOf(false) }

    val methods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. Title & Description ---
        CyberSectionHeader(
            title = AppStrings.composerTitle(lang),
            icon = Icons.Default.Send
        )

        // --- 2. Request URL & Method Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Method Dropdown
            Box {
                Button(
                    onClick = { showMethodDropdown = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getMethodColor(method).copy(alpha = 0.2f),
                        contentColor = getMethodColor(method)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = method,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CodeFont,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMethodDropdown,
                    onDismissRequest = { showMethodDropdown = false },
                    modifier = Modifier.background(CyberSurface)
                ) {
                    methods.forEach { m ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = m,
                                    color = getMethodColor(m),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CodeFont
                                )
                            },
                            onClick = {
                                viewModel.composerMethod.value = m
                                showMethodDropdown = false
                            }
                        )
                    }
                }
            }

            // URL input
            OutlinedTextField(
                value = url,
                onValueChange = { viewModel.composerUrl.value = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("https://api.example.com/endpoint", color = CyberTextMuted, fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = CyberTextPrimary,
                    fontFamily = CodeFont
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedContainerColor = CyberSurfaceVariant,
                    unfocusedContainerColor = CyberSurfaceVariant
                )
            )

            // Send Button
            Button(
                onClick = { viewModel.sendComposerRequest() },
                enabled = !isLoading && url.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color(0xFF00363D)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF00363D)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- 3. Sub Tabs (Headers, Body, Response) ---
        ScrollableTabRow(
            selectedTabIndex = activeSection.ordinal,
            containerColor = CyberSurface,
            contentColor = CyberCyan,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSection.ordinal]),
                    color = CyberCyan
                )
            }
        ) {
            Tab(
                selected = activeSection == ComposerSection.HEADERS,
                onClick = { activeSection = ComposerSection.HEADERS },
                text = { Text("${AppStrings.tabReqHeaders(lang)} (${headers.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = activeSection == ComposerSection.BODY,
                onClick = { activeSection = ComposerSection.BODY },
                text = { Text(AppStrings.tabPayload(lang), fontSize = 12.sp) }
            )
            Tab(
                selected = activeSection == ComposerSection.RESPONSE,
                onClick = { activeSection = ComposerSection.RESPONSE },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(AppStrings.tabResponse(lang), fontSize = 12.sp)
                        if (response != null) {
                            StatusCodeBadge(code = response!!.statusCode)
                        }
                    }
                }
            )
        }

        // --- 4. Tab Body Content ---
        when (activeSection) {
            ComposerSection.HEADERS -> {
                CyberCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.tabReqHeaders(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan
                        )
                        OutlinedButton(
                            onClick = { viewModel.addComposerHeader() },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.addHeader(lang), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (headers.isEmpty()) {
                        Text("No Headers added", color = CyberTextMuted, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            headers.forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = entry.key,
                                        onValueChange = { viewModel.updateComposerHeader(entry.id, it, entry.value) },
                                        placeholder = { Text("Header-Key", fontSize = 11.sp, color = CyberTextMuted) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = CyberTextPrimary, fontFamily = CodeFont),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = CyberCardBorder
                                        )
                                    )

                                    OutlinedTextField(
                                        value = entry.value,
                                        onValueChange = { viewModel.updateComposerHeader(entry.id, entry.key, it) },
                                        placeholder = { Text("Header-Value", fontSize = 11.sp, color = CyberTextMuted) },
                                        modifier = Modifier.weight(1.3f),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = CyberTextPrimary, fontFamily = CodeFont),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = CyberCardBorder
                                        )
                                    )

                                    IconButton(
                                        onClick = { viewModel.removeComposerHeader(entry.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberRose, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ComposerSection.BODY -> {
                CyberCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.tabPayload(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan
                        )
                        OutlinedButton(
                            onClick = { viewModel.formatComposerJson() },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberAmber)
                        ) {
                            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.formatJson(lang), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = body,
                        onValueChange = { viewModel.composerBody.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp),
                        placeholder = { Text(AppStrings.requestBodyHint(lang), fontSize = 12.sp, color = CyberTextMuted) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextPrimary,
                            fontFamily = CodeFont
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedContainerColor = Color(0xFF090D16),
                            unfocusedContainerColor = Color(0xFF090D16)
                        )
                    )
                }
            }

            ComposerSection.RESPONSE -> {
                if (error != null) {
                    CyberCard(borderColor = CyberRose) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = CyberRose)
                            Text(text = error!!, color = CyberRose, fontSize = 13.sp)
                        }
                    }
                } else if (response != null) {
                    val res = response!!
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Response Status Card
                        CyberCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatusCodeBadge(code = res.statusCode, statusText = res.statusMessage)
                                    Text(
                                        text = "${res.durationMs} ms",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontFamily = CodeFont
                                    )
                                    Text(
                                        text = "${res.contentLength} bytes",
                                        color = CyberTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = CodeFont
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.copyToClipboard(res.body, "Response") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Response Body Viewer
                        JsonViewer(
                            jsonString = res.body,
                            onCopy = { viewModel.copyToClipboard(it, "Response") },
                            emptyMessage = "Empty Response Body"
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Send a request to inspect the live response.",
                            color = CyberTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
