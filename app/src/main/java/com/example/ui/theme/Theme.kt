package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = CyberSurfaceLight,
    onPrimaryContainer = CyberCyan,
    secondary = CyberEmerald,
    onSecondary = Color.Black,
    secondaryContainer = CyberSurface,
    onSecondaryContainer = CyberEmerald,
    tertiary = CyberCyanDim,
    background = CyberBgDark,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceLight,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberCardBorder,
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
