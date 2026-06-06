package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NexusPrimary,
    secondary = NexusAccentCyan,
    tertiary = NexusPlusColor,
    background = NexusBackground,
    surface = NexusSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = NexusTextPrimary,
    onSurface = NexusTextPrimary,
    surfaceVariant = Color(0xFF1E2B38),
    onSurfaceVariant = Color(0xFFC4D1EC)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    secondary = Color(0xFF006A7B),
    tertiary = Color(0xFF7E39D0),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1D20),
    onSurface = Color(0xFF1C1D20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for premium Telegram look
    dynamicColor: Boolean = false, // Disable dynamic colors to ensure NEXUS branding remains intact
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
