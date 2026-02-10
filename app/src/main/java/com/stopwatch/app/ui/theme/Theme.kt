package com.stopwatch.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),       // teal
    secondary = Color(0xFFFF8A65),     // soft coral
    tertiary = Color(0xFFFFD54F),      // gold
    background = Color(0xFF0D1117),
    surface = Color(0xFF161B22),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00897B),       // teal
    secondary = Color(0xFFEF6C57),     // soft coral
    tertiary = Color(0xFFFFC107),      // gold
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
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
