package com.example.service.ui

import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.CrosshairConfig
import com.example.service.CrosshairStyle
import com.example.service.GameHudManager
import com.example.service.HudDisplayConfig
import com.example.service.HudMode
import com.example.service.VisionFilterConfig
import com.example.telemetry.TelemetryState
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FloatingSidePanelView(
    telemetryState: StateFlow<TelemetryState>,
    hudConfig: StateFlow<HudDisplayConfig>,
    visionConfig: StateFlow<VisionFilterConfig>,
    crosshairConfig: StateFlow<CrosshairConfig>,
    onClose: () -> Unit,
    onResetVision: () -> Unit,
    audioManager: AudioManager
) {
    val telemetry by telemetryState.collectAsState()
    val hud by hudConfig.collectAsState()
    val vision by visionConfig.collectAsState()
    val crosshair by crosshairConfig.collectAsState()

    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume)
    }

    // Outer container: Scrim + Side Drawer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClose() }
    ) {
        // Drawer content
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(320.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CarbonDark, CarbonBlack)
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(CyberCrimson.copy(alpha = 0.6f), Color.Transparent)),
                    RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks inside drawer */ }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(CyberCrimson)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GAME SPACE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Telemetry Dashboard Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TelemetryMetric(
                                label = "REAL FPS",
                                value = "${telemetry.currentFps}",
                                unit = "FPS",
                                color = if (telemetry.currentFps >= 50) CyberNeonGreen else CyberAmber
                            )
                            TelemetryMetric(
                                label = "REFRESH",
                                value = "${telemetry.refreshRateHz}",
                                unit = "Hz",
                                color = CyberCyan
                            )
                            TelemetryMetric(
                                label = "PING",
                                value = "${telemetry.pingMs}",
                                unit = "ms",
                                color = if (telemetry.pingMs < 50) CyberNeonGreen else CyberAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TelemetryMetric(
                                label = "BATTERY TEMP",
                                value = "${String.format("%.1f", telemetry.batteryTempCelsius)}",
                                unit = "°C",
                                color = if (telemetry.batteryTempCelsius > 42f) CyberCrimson else CyberCyan
                            )
                            TelemetryMetric(
                                label = "RAM USAGE",
                                value = "${telemetry.ramUsedPct}",
                                unit = "%",
                                color = Color.White
                            )
                            TelemetryMetric(
                                label = "SOC THERMAL",
                                value = telemetry.thermalStatusText,
                                unit = "",
                                color = CyberAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HUD Mode Switcher
                Text(
                    text = "HUD DISPLAY MODE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HudModeButton(
                        title = "Mini",
                        isSelected = hud.mode == HudMode.MINI,
                        modifier = Modifier.weight(1f)
                    ) {
                        GameHudManager.updateHudConfig { it.copy(mode = HudMode.MINI) }
                    }
                    HudModeButton(
                        title = "Gaming",
                        isSelected = hud.mode == HudMode.GAMING,
                        modifier = Modifier.weight(1f)
                    ) {
                        GameHudManager.updateHudConfig { it.copy(mode = HudMode.GAMING) }
                    }
                    HudModeButton(
                        title = "Advanced",
                        isSelected = hud.mode == HudMode.ADVANCED,
                        modifier = Modifier.weight(1f)
                    ) {
                        GameHudManager.updateHudConfig { it.copy(mode = HudMode.ADVANCED) }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Hunt Vision Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "HUNT VISION FILTERS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (vision.isEnabled) {
                        Text(
                            text = "Reset",
                            color = CyberCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onResetVision() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickToggleRow(
                        title = "Dark Vision (Shadow Gamma)",
                        subtitle = "Boosts dark shadows in PUBG",
                        isActive = vision.isEnabled && vision.darkVisionBoost > 0f,
                        onToggle = { active ->
                            if (active) {
                                GameHudManager.updateVisionConfig {
                                    it.copy(
                                        isEnabled = true,
                                        presetName = "Dark Vision",
                                        invertColors = false,
                                        darkVisionBoost = 0.75f,
                                        contrastLevel = 1.3f
                                    )
                                }
                            } else {
                                onResetVision()
                            }
                        }
                    )

                    QuickToggleRow(
                        title = "Negative / Invert Colors",
                        subtitle = "Inverts screen colors for target spotting",
                        isActive = vision.isEnabled && vision.invertColors,
                        onToggle = { active ->
                            if (active) {
                                GameHudManager.updateVisionConfig {
                                    it.copy(
                                        isEnabled = true,
                                        presetName = "Negative",
                                        invertColors = true,
                                        darkVisionBoost = 0f
                                    )
                                }
                            } else {
                                onResetVision()
                            }
                        }
                    )

                    QuickToggleRow(
                        title = "High Contrast Mode",
                        subtitle = "Sharper enemy outlines against background",
                        isActive = vision.isEnabled && vision.contrastLevel > 1.4f,
                        onToggle = { active ->
                            if (active) {
                                GameHudManager.updateVisionConfig {
                                    it.copy(
                                        isEnabled = true,
                                        presetName = "High Contrast",
                                        invertColors = false,
                                        contrastLevel = 1.8f,
                                        darkVisionBoost = 0.2f
                                    )
                                }
                            } else {
                                onResetVision()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tactical Aim Reticle Quick Switch
                Text(
                    text = "AIM TOOLS RETICLE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    QuickToggleRow(
                        title = "Floating Crosshair Reticle",
                        subtitle = "Zero touch latency overlay",
                        isActive = crosshair.isEnabled,
                        onToggle = { active ->
                            GameHudManager.updateCrosshairConfig { it.copy(isEnabled = active) }
                        }
                    )

                    if (crosshair.isEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ReticleStylePill("Cross", crosshair.style == CrosshairStyle.CROSS, Modifier.weight(1f)) {
                                GameHudManager.updateCrosshairConfig { it.copy(style = CrosshairStyle.CROSS) }
                            }
                            ReticleStylePill("Dot", crosshair.style == CrosshairStyle.DOT, Modifier.weight(1f)) {
                                GameHudManager.updateCrosshairConfig { it.copy(style = CrosshairStyle.DOT) }
                            }
                            ReticleStylePill("Circle", crosshair.style == CrosshairStyle.CIRCLE_DOT, Modifier.weight(1f)) {
                                GameHudManager.updateCrosshairConfig { it.copy(style = CrosshairStyle.CIRCLE_DOT) }
                            }
                            ReticleStylePill("T-Tactical", crosshair.style == CrosshairStyle.T_TACTICAL, Modifier.weight(1f)) {
                                GameHudManager.updateCrosshairConfig { it.copy(style = CrosshairStyle.T_TACTICAL) }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In-Game Media Volume Slider
                Text(
                    text = "GAME AUDIO LEVEL",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = currentVolume,
                        onValueChange = { newVol ->
                            currentVolume = newVol
                            val targetInt = (newVol * maxVolume).toInt()
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetInt, 0)
                            } catch (_: Exception) {}
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = CarbonBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(currentVolume * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TelemetryMetric(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    color = color.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HudModeButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCrimson else CarbonCard)
            .border(
                1.dp,
                if (isSelected) CyberCrimson else CarbonBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun QuickToggleRow(
    title: String,
    subtitle: String,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
        Switch(
            checked = isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyberCrimson,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CarbonDark
            )
        )
    }
}

@Composable
private fun ReticleStylePill(
    name: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyberCrimson.copy(alpha = 0.25f) else CarbonDark)
            .border(
                1.dp,
                if (isSelected) CyberCrimson else CarbonBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isSelected) CyberCrimson else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
