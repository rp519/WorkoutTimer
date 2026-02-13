package com.stopwatch.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Reusable gradient definitions for the WorkoutTimer app.
 * All gradients follow the premium blue design system.
 */
object Gradients {

    /**
     * Primary gradient: 135° angle, deep blue to light blue
     * Use for: Headers, app bars, primary buttons, splash screen
     */
    val Primary = Brush.linearGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) // 135° diagonal
    )

    /**
     * Secondary gradient: Lighter blue variation
     * Use for: Cards, secondary UI elements
     */
    val Secondary = Brush.linearGradient(
        colors = listOf(PrimaryGradientEnd, SecondaryBlue),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Vertical overlay gradient: Transparent to 80% black
     * Use for: Image overlays to ensure text readability
     */
    val ImageOverlay = Brush.verticalGradient(
        colors = listOf(Color.Transparent, OverlayGradient)
    )

    /**
     * Top overlay gradient: Black fade from top
     * Use for: Exercise images with text at top
     */
    val TopOverlay = Brush.verticalGradient(
        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
        startY = 0f,
        endY = 400f
    )

    /**
     * Bottom overlay gradient: Black fade from bottom
     * Use for: Exercise images with text at bottom
     */
    val BottomOverlay = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    /**
     * Subtle background gradient for light surfaces
     * Use for: Screen backgrounds
     */
    val BackgroundSubtle = Brush.verticalGradient(
        colors = listOf(
            BackgroundLightBlue,
            Color.White
        )
    )
}
