package com.stopwatch.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGradientEnd,           // Light blue
    onPrimary = Color.White,
    primaryContainer = PrimaryGradientStart, // Deep blue
    onPrimaryContainer = Color.White,

    secondary = SecondaryBlue,
    onSecondary = TextPrimaryDark,

    tertiary = AccentAmber,
    onTertiary = Color.White,

    background = Color(0xFF0D1117),
    onBackground = Color.White,

    surface = Color(0xFF161B22),
    onSurface = Color.White,

    surfaceVariant = Color(0xFF1C2128),
    onSurfaceVariant = Color(0xFFB0B8C0),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGradientStart,          // Deep blue
    onPrimary = Color.White,
    primaryContainer = PrimaryGradientEnd,   // Light blue
    onPrimaryContainer = TextPrimaryDark,

    secondary = SecondaryBlue,
    onSecondary = TextPrimaryDark,

    tertiary = AccentAmber,
    onTertiary = Color.White,

    background = BackgroundLightBlue,        // Light blue background
    onBackground = TextPrimaryDark,

    surface = Color.White,
    onSurface = TextPrimaryDark,

    surfaceVariant = Color(0xFFF0F4F8),
    onSurfaceVariant = TextSecondary,

    error = Color(0xFFD32F2F),
    onError = Color.White,
)

@Composable
fun WorkoutTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
