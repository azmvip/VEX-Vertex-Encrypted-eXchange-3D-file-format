package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = CyberViolet,
    onSecondary = Color(0xFF2C0064),
    secondaryContainer = Color(0xFF451E84),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = CyberGreen,
    onTertiary = Color(0xFF003820),
    tertiaryContainer = Color(0xFF005232),
    onTertiaryContainer = Color(0xFF6CFFA9),
    background = CyberBg,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberCardBorder,
    error = CyberRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}
