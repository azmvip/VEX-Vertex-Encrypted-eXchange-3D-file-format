package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PulsingRecordingIndicator
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.ReqInspectViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ReqInspectViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val activeTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg, withDismissAction = true)
        }
    }

    val layoutDirection = if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalAppLanguage provides lang,
        LocalLayoutDirection provides layoutDirection
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(CyberBg),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = CyberSurfaceVariant,
                        contentColor = CyberCyan,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ReqInspect",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = CodeFont
                            )
                            Text(
                                text = "DevTools",
                                color = CyberViolet,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                fontFamily = CodeFont
                            )
                        }
                    },
                    actions = {
                        // Language switcher pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberSurfaceVariant)
                                .clickable {
                                    val nextLang = if (lang == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
                                    viewModel.setLanguage(nextLang)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (lang == AppLanguage.ARABIC) "EN" else "عربي",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Recording toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleRecording() }
                        ) {
                            PulsingRecordingIndicator(isRecording = isRecording)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberSurface,
                        titleContentColor = CyberCyan
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CyberSurface,
                    contentColor = CyberCyan,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val navItems = listOf(
                        Triple(AppTab.BROWSER, Icons.Default.Language, AppStrings.tabBrowser(lang)),
                        Triple(AppTab.INSPECTOR, Icons.Default.Search, AppStrings.tabInspector(lang)),
                        Triple(AppTab.COMPOSER, Icons.Default.Send, AppStrings.tabComposer(lang)),
                        Triple(AppTab.MOCK_RULES, Icons.Default.Rule, AppStrings.tabRules(lang)),
                        Triple(AppTab.MEDIA, Icons.Default.VideoLibrary, AppStrings.tabMedia(lang)),
                        Triple(AppTab.CONSOLE, Icons.Default.Terminal, AppStrings.tabConsole(lang)),
                        Triple(AppTab.SESSIONS, Icons.Default.Settings, AppStrings.tabSessions(lang))
                    )

                    navItems.forEach { (tab, icon, label) ->
                        val isSelected = activeTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(tab) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (tab == AppTab.INSPECTOR && allRequests.isNotEmpty()) {
                                            Badge(
                                                containerColor = CyberCyan,
                                                contentColor = Color(0xFF00363D)
                                            ) {
                                                Text(
                                                    text = if (allRequests.size > 99) "99+" else allRequests.size.toString(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00363D),
                                selectedTextColor = CyberCyan,
                                unselectedIconColor = CyberTextMuted,
                                unselectedTextColor = CyberTextMuted,
                                indicatorColor = CyberCyan
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (activeTab) {
                    AppTab.BROWSER -> BrowserScreen(viewModel = viewModel, lang = lang)
                    AppTab.INSPECTOR -> InspectorScreen(viewModel = viewModel, lang = lang)
                    AppTab.COMPOSER -> ComposerScreen(viewModel = viewModel, lang = lang)
                    AppTab.MOCK_RULES -> MockRulesScreen(viewModel = viewModel, lang = lang)
                    AppTab.MEDIA -> MediaSnifferScreen(viewModel = viewModel, lang = lang)
                    AppTab.CONSOLE -> ConsoleScreen(viewModel = viewModel, lang = lang)
                    AppTab.SESSIONS -> SessionsScreen(viewModel = viewModel, lang = lang)
                }
            }
        }
    }
}
