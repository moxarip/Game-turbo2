package com.example.service.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.example.service.VisionFilterConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * Floating overlay filter for Hunt Vision.
 * Runs non-intrusively in a touch-transparent overlay window (FLAG_NOT_TOUCHABLE).
 * Applies color transformation, gamma enhancement, shadow brightening, and negative simulation.
 */
@Composable
fun FloatingVisionFilterView(
    config: StateFlow<VisionFilterConfig>
) {
    val filter by config.collectAsState()
    if (!filter.isEnabled) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (filter.invertColors) {
            // Negative / Invert filter overlay simulation
            // Draw a high-contrast inverse luminance layer with Difference blend mode
            drawRect(
                color = Color.White.copy(alpha = 0.92f),
                blendMode = BlendMode.Difference
            )
        } else if (filter.darkVisionBoost > 0f) {
            // Dark Vision / Black Equalizer:
            // Brightens deep shadows by applying a soft gamma curve tint with Screen blend mode
            val boostAlpha = (filter.darkVisionBoost * 0.35f).coerceIn(0.05f, 0.45f)
            drawRect(
                color = Color(0xFF408080).copy(alpha = boostAlpha),
                blendMode = BlendMode.Screen
            )
        } else if (filter.contrastLevel > 1.2f) {
            // High Contrast mode:
            // Deepens shadows and clarifies midtones with Overlay blend mode
            val contrastAlpha = ((filter.contrastLevel - 1.0f) * 0.3f).coerceIn(0.05f, 0.40f)
            drawRect(
                color = Color(0xFF101015).copy(alpha = contrastAlpha),
                blendMode = BlendMode.Overlay
            )
        } else if (filter.saturationLevel > 1.3f) {
            // Vibrant mode:
            // Adds rich saturation boost tint
            val satAlpha = ((filter.saturationLevel - 1.0f) * 0.18f).coerceIn(0.05f, 0.25f)
            drawRect(
                color = Color(0xFFFF4081).copy(alpha = satAlpha),
                blendMode = BlendMode.Color
            )
        }
    }
}
