package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InterceptedRequestEntity
import com.example.ui.components.*
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReqInspectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorScreen(
    viewModel: ReqInspectViewModel,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val requests by viewModel.filteredRequests.collectAsStateWithLifecycle()
    val totalRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterMethod by viewModel.filterMethod.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedRequest by viewModel.selectedRequest.collectAsStateWithLifecycle()

    var showMethodDropdown by remember { mutableStateOf(false) }

    val filterChips = listOf(
        "ALL" to AppStrings.filterAll(lang),
        "XHR_FETCH" to AppStrings.filterXhrFetch(lang),
        "DOC" to AppStrings.filterDoc(lang),
        "MEDIA" to AppStrings.filterMedia(lang),
        "JS" to AppStrings.filterJs(lang),
        "CSS" to AppStrings.filterCss(lang),
        "IMG" to AppStrings.filterImg(lang),
        "ERRORS" to AppStrings.filterErrors(lang)
    )

    val methodsList = listOf("ALL", "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // --- 1. Search & Filter Bar ---
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = AppStrings.searchRequests(lang),
                            color = CyberTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = CyberTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberTextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    )
                )

                // Method Selector Box
                Box {
                    OutlinedButton(
                        onClick = { showMethodDropdown = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CyberSurfaceVariant,
                            contentColor = CyberCyan
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filterMethod,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CodeFont
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Method Dropdown",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMethodDropdown,
                        onDismissRequest = { showMethodDropdown = false },
                        modifier = Modifier.background(CyberSurface)
                    ) {
                        methodsList.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = m,
                                        color = if (filterMethod == m) CyberCyan else CyberTextPrimary,
                                        fontFamily = CodeFont,
                                        fontWeight = if (filterMethod == m) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.filterMethod.value = m
                                    showMethodDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Filter Chips Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterChips.forEach { (typeKey, label) ->
                    val isSelected = filterType == typeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterType.value = typeKey },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyberCyan else CyberTextSecondary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.18f),
                            containerColor = CyberSurfaceVariant
                        ),
                        border = null
                    )
                }
            }

            // Stats & Controls Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleRecording() }
                    ) {
                        PulsingRecordingIndicator(isRecording = isRecording)
                    }

                    Text(
                        text = "${requests.size} / ${totalRequests.size}",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = CodeFont
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Clear logs button
                    TextButton(
                        onClick = { viewModel.clearAllRequests() },
                        colors = ButtonDefaults.textButtonColors(contentColor = CyberRose)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.clearAllLogs(lang), fontSize = 11.sp)
                    }
                }
            }
        }

        // --- 2. Requests Feed ---
        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar",
                        tint = CyberCyan.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = AppStrings.noRequestsCaptured(lang),
                        color = CyberTextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requests, key = { it.id }) { request ->
                    RequestItemCard(
                        request = request,
                        onClick = { viewModel.selectRequest(request) }
                    )
                }
            }
        }
    }

    // Modal Details Sheet
    selectedRequest?.let { req ->
        RequestDetailsSheet(
            request = req,
            viewModel = viewModel,
            lang = lang,
            onDismiss = { viewModel.selectRequest(null) }
        )
    }
}

@Composable
fun RequestItemCard(
    request: InterceptedRequestEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier,
        onClick = onClick,
        borderColor = if (request.isMocked) CyberViolet.copy(alpha = 0.6f) else if (request.isBlocked) CyberRose.copy(alpha = 0.6f) else CyberCardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MethodBadge(method = request.method)
            StatusCodeBadge(code = request.statusCode, statusText = "")
            ResourceTypeBadge(type = request.resourceType)

            Spacer(modifier = Modifier.weight(1f))

            // Duration & Size
            if (request.durationMs > 0) {
                Text(
                    text = "${request.durationMs}ms",
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontFamily = CodeFont
                )
            }
            if (request.contentLength > 0) {
                Text(
                    text = formatBytes(request.contentLength),
                    color = CyberTextMuted,
                    fontSize = 10.sp,
                    fontFamily = CodeFont
                )
            }
            if (request.isBookmarked) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Bookmarked",
                    tint = CyberAmber,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Path & Host
        Text(
            text = request.path,
            color = CyberTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = CodeFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = request.host,
            color = CyberTextSecondary,
            fontSize = 11.sp,
            fontFamily = CodeFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
