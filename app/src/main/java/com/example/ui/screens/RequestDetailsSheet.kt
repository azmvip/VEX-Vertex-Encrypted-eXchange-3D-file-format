package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InterceptedRequestEntity
import com.example.engine.CurlGenerator
import com.example.ui.components.*
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReqInspectViewModel

enum class RequestDetailTab {
    GENERAL,
    REQ_HEADERS,
    RES_HEADERS,
    PARAMS,
    PAYLOAD,
    RESPONSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsSheet(
    request: InterceptedRequestEntity,
    viewModel: ReqInspectViewModel,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(RequestDetailTab.GENERAL) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = CyberCyan.copy(alpha = 0.5f))
        },
        modifier = modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header: Method, Status, Host, Path
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MethodBadge(method = request.method)
                StatusCodeBadge(code = request.statusCode, statusText = request.statusText)
                ResourceTypeBadge(type = request.resourceType)

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { viewModel.toggleBookmark(request) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (request.isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Bookmark",
                        tint = if (request.isBookmarked) CyberAmber else CyberTextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.deleteRequest(request) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CyberRose
                    )
                }
            }

            // URL Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant.copy(alpha = 0.7f))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = request.url,
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = CodeFont,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.copyToClipboard(request.url, "URL") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy URL",
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Action Buttons Row (Replay in Composer, Copy cURL, Mock Rule)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Send to Composer (Replay)
                Button(
                    onClick = {
                        viewModel.populateComposerFromRequest(request)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color(0xFF00363D),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.sendToComposer(lang),
                        color = Color(0xFF00363D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Copy as cURL
                OutlinedButton(
                    onClick = {
                        val curl = CurlGenerator.generateCurl(request)
                        viewModel.copyToClipboard(curl, "cURL")
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberGreen),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberGreen)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.copyCurl(lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Create Mock Rule
                OutlinedButton(
                    onClick = {
                        viewModel.createRuleFromRequest(request)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberViolet),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberViolet)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Rule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.createMockRule(lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Multi-tabs row
            ScrollableTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = CyberCyan,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                        color = CyberCyan
                    )
                }
            ) {
                Tab(
                    selected = activeTab == RequestDetailTab.GENERAL,
                    onClick = { activeTab = RequestDetailTab.GENERAL },
                    text = { Text(AppStrings.tabGeneral(lang), fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == RequestDetailTab.REQ_HEADERS,
                    onClick = { activeTab = RequestDetailTab.REQ_HEADERS },
                    text = { Text("${AppStrings.tabReqHeaders(lang)} (${request.getRequestHeadersMap().size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == RequestDetailTab.RES_HEADERS,
                    onClick = { activeTab = RequestDetailTab.RES_HEADERS },
                    text = { Text("${AppStrings.tabResHeaders(lang)} (${request.getResponseHeadersMap().size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == RequestDetailTab.PAYLOAD,
                    onClick = { activeTab = RequestDetailTab.PAYLOAD },
                    text = { Text(AppStrings.tabPayload(lang), fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == RequestDetailTab.RESPONSE,
                    onClick = { activeTab = RequestDetailTab.RESPONSE },
                    text = { Text(AppStrings.tabResponse(lang), fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    RequestDetailTab.GENERAL -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KeyValueRow(
                                keyText = "URL",
                                valueText = request.url,
                                onCopy = { viewModel.copyToClipboard(request.url, "URL") }
                            )
                            KeyValueRow(keyText = "Method", valueText = request.method)
                            KeyValueRow(keyText = "Status", valueText = "${request.statusCode} ${request.statusText}")
                            KeyValueRow(keyText = "Host", valueText = request.host)
                            KeyValueRow(keyText = "Path", valueText = request.path)
                            KeyValueRow(keyText = "Resource Type", valueText = request.resourceType)
                            KeyValueRow(keyText = "Duration / Latency", valueText = "${request.durationMs} ms")
                            KeyValueRow(keyText = "Content Length", valueText = "${request.contentLength} bytes")
                            KeyValueRow(keyText = "Timestamp", valueText = java.util.Date(request.timestamp).toString())
                            if (request.isMocked) {
                                KeyValueRow(keyText = "Mock Engine", valueText = "Response was intercepted & mocked by rule")
                            }
                            if (request.isBlocked) {
                                KeyValueRow(keyText = "Block Engine", valueText = "Request blocked by rule")
                            }
                        }
                    }

                    RequestDetailTab.REQ_HEADERS -> {
                        val headers = request.getRequestHeadersMap()
                        if (headers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No Request Headers Recorded", color = CyberTextMuted, fontSize = 13.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                headers.forEach { (k, v) ->
                                    KeyValueRow(
                                        keyText = k,
                                        valueText = v,
                                        onCopy = { viewModel.copyToClipboard("$k: $v", "Header") }
                                    )
                                }
                            }
                        }
                    }

                    RequestDetailTab.RES_HEADERS -> {
                        val headers = request.getResponseHeadersMap()
                        if (headers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No Response Headers Recorded", color = CyberTextMuted, fontSize = 13.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                headers.forEach { (k, v) ->
                                    KeyValueRow(
                                        keyText = k,
                                        valueText = v,
                                        onCopy = { viewModel.copyToClipboard("$k: $v", "Header") }
                                    )
                                }
                            }
                        }
                    }

                    RequestDetailTab.PAYLOAD -> {
                        JsonViewer(
                            jsonString = request.requestBody,
                            onCopy = { viewModel.copyToClipboard(it, "Request Payload") },
                            emptyMessage = "No Request Body / Payload"
                        )
                    }

                    RequestDetailTab.RESPONSE -> {
                        JsonViewer(
                            jsonString = request.responseBody,
                            onCopy = { viewModel.copyToClipboard(it, "Response Body") },
                            emptyMessage = "No Response Body Captured"
                        )
                    }

                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
