package com.example.service.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.HudDisplayConfig
import com.example.service.HudMode
import com.example.telemetry.TelemetryState
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNeonGreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FloatingHudPillView(
    telemetryState: StateFlow<TelemetryState>,
    hudConfig: StateFlow<HudDisplayConfig>,
    onPillClick: () -> Unit
) {
    val telemetry by telemetryState.collectAsState()
    val config by hudConfig.collectAsState()

    val accentColor = Color(config.accentColorArgb)

    val fpsColor by animateColorAsState(
        targetValue = when {
            telemetry.currentFps >= 55 -> CyberNeonGreen
            telemetry.currentFps >= 40 -> CyberAmber
            else -> CyberCrimson
        },
        label = "fps_color"
    )

    val tempColor = when {
        telemetry.batteryTempCelsius >= 43.0f -> CyberCrimson
        telemetry.batteryTempCelsius >= 38.0f -> CyberAmber
        else -> CyberCyan
    }

    val alphaBackground = CarbonBlack.copy(alpha = config.opacity)

    when (config.mode) {
        HudMode.MINI -> {
            // Ultra-compact Pill for minimal game obstruction
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(alphaBackground)
                    .border(1.2.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .clickable { onPillClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(fpsColor)
                    )
                    Text(
                        text = "${telemetry.currentFps} FPS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format("%.1f", telemetry.batteryTempCelsius)}°C",
                        color = tempColor,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }
        else -> {
            // Gaming & Advanced Tactical Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(alphaBackground)
                    .border(1.5.dp, accentColor, RoundedCornerShape(14.dp))
                    .clickable { onPillClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Game Space Logo / Accent Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor)
                    )

                    // FPS Counter
                    if (config.showFps) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "FPS",
                                tint = fpsColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${telemetry.currentFps}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                            Text(
                                text = " FPS",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Display Refresh Rate
                    if (config.showRefreshRate) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(0.8.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${telemetry.refreshRateHz}Hz",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Battery Temperature
                    if (config.showBatteryTemp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Temp",
                                tint = tempColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${String.format("%.1f", telemetry.batteryTempCelsius)}°C",
                                color = tempColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Battery Level
                    if (config.showBattery) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Battery",
                                tint = if (telemetry.isCharging) CyberNeonGreen else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${telemetry.batteryPct}%",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Network Ping
                    if (config.showPing && telemetry.pingMs > 0) {
                        val pingColor = when {
                            telemetry.pingMs < 45 -> CyberNeonGreen
                            telemetry.pingMs < 90 -> CyberAmber
                            else -> CyberCrimson
                        }
                        Text(
                            text = "${telemetry.pingMs}ms",
                            color = pingColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
