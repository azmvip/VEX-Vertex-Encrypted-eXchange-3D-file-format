package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MockRuleEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.StatusCodeBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReqInspectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockRulesScreen(
    viewModel: ReqInspectViewModel,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.activeRules.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<MockRuleEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(12.dp)
    ) {
        // Header
        CyberSectionHeader(
            title = AppStrings.rulesTitle(lang),
            icon = Icons.Default.Rule,
            action = {
                Button(
                    onClick = {
                        editingRule = null
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.addNewRule(lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (rules.isEmpty()) {
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
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = null,
                        tint = CyberViolet.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "No Mock / Breakpoint Rules configured yet.\nCreate rules to mock API responses or block ads and trackers in the embedded browser.",
                        color = CyberTextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    CyberCard(
                        borderColor = if (rule.isEnabled) CyberViolet.copy(alpha = 0.5f) else CyberCardBorder
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = rule.name,
                                        color = if (rule.isEnabled) CyberTextPrimary else CyberTextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (rule.actionType == "block") CyberRose.copy(alpha = 0.2f) else CyberViolet.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (rule.actionType == "block") "BLOCK" else "MOCK ${rule.mockStatusCode}",
                                            color = if (rule.actionType == "block") CyberRose else CyberViolet,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = CodeFont
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pattern: ${rule.urlPattern} (${rule.matchType})",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontFamily = CodeFont
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = rule.isEnabled,
                                    onCheckedChange = { viewModel.toggleRule(rule) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CyberViolet,
                                        checkedTrackColor = CyberViolet.copy(alpha = 0.4f)
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        editingRule = rule
                                        showAddDialog = true
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.deleteRule(rule) }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberRose, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Rule Modal Dialog
    if (showAddDialog) {
        var ruleName by remember { mutableStateOf(editingRule?.name ?: "") }
        var urlPattern by remember { mutableStateOf(editingRule?.urlPattern ?: "") }
        var matchType by remember { mutableStateOf(editingRule?.matchType ?: "contains") }
        var actionType by remember { mutableStateOf(editingRule?.actionType ?: "mock_response") }
        var statusCodeStr by remember { mutableStateOf((editingRule?.mockStatusCode ?: 200).toString()) }
        var mockBody by remember { mutableStateOf(editingRule?.mockResponseBody ?: "{\n  \"mocked\": true,\n  \"status\": \"success\"\n}") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CyberSurface,
            title = {
                Text(
                    text = if (editingRule == null) AppStrings.addNewRule(lang) else "Edit Rule",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ruleName,
                        onValueChange = { ruleName = it },
                        label = { Text(AppStrings.ruleName(lang)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCardBorder
                        )
                    )

                    OutlinedTextField(
                        value = urlPattern,
                        onValueChange = { urlPattern = it },
                        label = { Text(AppStrings.urlPattern(lang)) },
                        placeholder = { Text("e.g. /api/users or ads.tracker.com") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCardBorder
                        )
                    )

                    // Action Type Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = actionType == "mock_response",
                            onClick = { actionType = "mock_response" },
                            label = { Text("Mock Response", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberViolet.copy(alpha = 0.2f))
                        )
                        FilterChip(
                            selected = actionType == "block",
                            onClick = { actionType = "block" },
                            label = { Text("Block", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberRose.copy(alpha = 0.2f))
                        )
                    }

                    if (actionType == "mock_response") {
                        OutlinedTextField(
                            value = statusCodeStr,
                            onValueChange = { statusCodeStr = it },
                            label = { Text(AppStrings.statusCode(lang)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberCardBorder
                            )
                        )

                        OutlinedTextField(
                            value = mockBody,
                            onValueChange = { mockBody = it },
                            label = { Text(AppStrings.mockResponseBody(lang)) },
                            modifier = Modifier.heightIn(min = 120.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = CodeFont),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberCardBorder
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedStatus = statusCodeStr.toIntOrNull() ?: 200
                        val newRule = MockRuleEntity(
                            id = editingRule?.id ?: 0L,
                            name = if (ruleName.isBlank()) "Rule for $urlPattern" else ruleName,
                            urlPattern = urlPattern,
                            matchType = matchType,
                            isEnabled = editingRule?.isEnabled ?: true,
                            actionType = actionType,
                            mockStatusCode = parsedStatus,
                            mockResponseBody = mockBody,
                            mockHeadersJson = "{\"Content-Type\": \"application/json\", \"X-Mocked\": \"ReqInspect\"}"
                        )
                        viewModel.saveRule(newRule)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text(AppStrings.save(lang), color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(AppStrings.cancel(lang), color = CyberTextMuted)
                }
            }
        )
    }
}
