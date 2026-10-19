package com.example.service.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.service.CrosshairConfig
import com.example.service.CrosshairStyle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FloatingCrosshairView(
    config: StateFlow<CrosshairConfig>
) {
    val crosshair by config.collectAsState()
    if (!crosshair.isEnabled) return

    val color = Color(crosshair.colorArgb).copy(alpha = crosshair.opacity)
    val sizeDp = crosshair.sizeDp.dp
    val strokeWidthPx = (crosshair.thicknessDp * 2.5f)
    val gapPx = (crosshair.centerGapDp * 2.5f)

    Box(
        modifier = Modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            when (crosshair.style) {
                CrosshairStyle.DOT -> {
                    // Center dot with high visibility halo
                    drawCircle(
                        color = Color.Black.copy(alpha = crosshair.opacity * 0.7f),
                        radius = (crosshair.thicknessDp * 2.2f) + 1.5f,
                        center = center
                    )
                    drawCircle(
                        color = color,
                        radius = (crosshair.thicknessDp * 2.2f),
                        center = center
                    )
                }

                CrosshairStyle.CIRCLE -> {
                    drawCircle(
                        color = color,
                        radius = radius * 0.7f,
                        center = center,
                        style = Stroke(width = strokeWidthPx)
                    )
                }

                CrosshairStyle.CROSS -> {
                    // 4 cross arms with center gap
                    // Top
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y - gapPx),
                        end = Offset(center.x, center.y - radius),
                        strokeWidth = strokeWidthPx
                    )
                    // Bottom
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y + gapPx),
                        end = Offset(center.x, center.y + radius),
                        strokeWidth = strokeWidthPx
                    )
                    // Left
                    drawLine(
                        color = color,
                        start = Offset(center.x - gapPx, center.y),
                        end = Offset(center.x - radius, center.y),
                        strokeWidth = strokeWidthPx
                    )
                    // Right
                    drawLine(
                        color = color,
                        start = Offset(center.x + gapPx, center.y),
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = strokeWidthPx
                    )
                }

                CrosshairStyle.CIRCLE_DOT -> {
                    // Tactical Circle + Dot + ticks
                    drawCircle(
                        color = color,
                        radius = radius * 0.65f,
                        center = center,
                        style = Stroke(width = strokeWidthPx * 0.85f)
                    )
                    drawCircle(
                        color = color,
                        radius = strokeWidthPx * 0.9f,
                        center = center
                    )
                    // Cardinal ticks
                    val tickLen = radius * 0.25f
                    drawLine(color, Offset(center.x, center.y - radius * 0.65f), Offset(center.x, center.y - radius * 0.65f - tickLen), strokeWidthPx)
                    drawLine(color, Offset(center.x, center.y + radius * 0.65f), Offset(center.x, center.y + radius * 0.65f + tickLen), strokeWidthPx)
                    drawLine(color, Offset(center.x - radius * 0.65f, center.y), Offset(center.x - radius * 0.65f - tickLen, center.y), strokeWidthPx)
                    drawLine(color, Offset(center.x + radius * 0.65f, center.y), Offset(center.x + radius * 0.65f + tickLen, center.y), strokeWidthPx)
                }

                CrosshairStyle.T_TACTICAL -> {
                    // Tactical T-Shape (clear top view)
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y + gapPx),
                        end = Offset(center.x, center.y + radius),
                        strokeWidth = strokeWidthPx
                    )
                    drawLine(
                        color = color,
                        start = Offset(center.x - gapPx, center.y),
                        end = Offset(center.x - radius, center.y),
                        strokeWidth = strokeWidthPx
                    )
                    drawLine(
                        color = color,
                        start = Offset(center.x + gapPx, center.y),
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = strokeWidthPx
                    )
                    drawCircle(
                        color = color,
                        radius = strokeWidthPx * 0.7f,
                        center = center
                    )
                }

                CrosshairStyle.CHEVRON -> {
                    // Inverted V Apex Chevron
                    val path = Path().apply {
                        moveTo(center.x - radius * 0.6f, center.y + radius * 0.4f)
                        lineTo(center.x, center.y - radius * 0.3f)
                        lineTo(center.x + radius * 0.6f, center.y + radius * 0.4f)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = strokeWidthPx)
                    )
                }
            }
        }
    }
}
