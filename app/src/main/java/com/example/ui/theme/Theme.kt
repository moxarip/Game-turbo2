package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GameCoreDarkColorScheme = darkColorScheme(
    primary = CyberCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B070E),
    onPrimaryContainer = CyberCrimsonGlow,
    secondary = CyberCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00363D),
    onSecondaryContainer = CyberCyan,
    tertiary = CyberAmber,
    onTertiary = Color.Black,
    background = CarbonBlack,
    onBackground = TextPrimary,
    surface = CarbonDark,
    onSurface = TextPrimary,
    surfaceVariant = CarbonSurface,
    onSurfaceVariant = TextSecondary,
    outline = CarbonBorder,
    outlineVariant = Color(0xFF1E212B)
)

@Composable
fun GameCoreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameCoreDarkColorScheme,
        typography = Typography,
        content = content
    )
}
