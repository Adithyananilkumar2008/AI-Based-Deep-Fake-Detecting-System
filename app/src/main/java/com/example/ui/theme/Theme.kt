package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = CyberDark,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = CyanGlow,
    secondary = MintTeal,
    onSecondary = CyberDark,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = MintTeal,
    tertiary = AmberAlert,
    onTertiary = CyberDark,
    error = DangerRed,
    onError = Color.White,
    background = CyberDark,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek forensic dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
