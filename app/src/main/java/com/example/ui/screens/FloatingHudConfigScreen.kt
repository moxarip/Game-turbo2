package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.HudMode
import com.example.service.ui.FloatingHudPillView
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
import com.example.ui.viewmodel.GameCoreViewModel

@Composable
fun FloatingHudConfigScreen(viewModel: GameCoreViewModel) {
    val hudConfig by viewModel.hudConfig.collectAsState()
    val telemetryState = viewModel.telemetryState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Column {
                Text(
                    text = "FLOATING HUD STUDIO",
                    color = CyberCrimson,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "In-Game Telemetry Overlay",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Displays real-time hardware metrics without interfering with game controls.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Interactive Live Preview Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1B1E29), CarbonDark)
                        )
                    )
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Live preview of the pill
                FloatingHudPillView(
                    telemetryState = telemetryState,
                    hudConfig = viewModel.hudConfig,
                    onPillClick = { /* Preview click */ }
                )

                Text(
                    text = "LIVE HUD PREVIEW",
                    color = TextMuted.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }
        }

        // HUD Style Mode Selection
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HUD PROFILE MODE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeCard(
                        title = "Mini",
                        description = "FPS & Temp only",
                        isSelected = hudConfig.mode == HudMode.MINI,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setHudMode(HudMode.MINI) }

                    ModeCard(
                        title = "Gaming",
                        description = "FPS, Refresh, RAM",
                        isSelected = hudConfig.mode == HudMode.GAMING,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setHudMode(HudMode.GAMING) }

                    ModeCard(
                        title = "Advanced",
                        description = "Full telemetry matrix",
                        isSelected = hudConfig.mode == HudMode.ADVANCED,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setHudMode(HudMode.ADVANCED) }
                }
            }
        }

        // Appearance: Opacity & Accent Color
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APPEARANCE & THEME",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Accent Color Selector
                Text("HUD Accent Color", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val colors = listOf(
                        "Crimson" to CyberCrimson,
                        "Cyan" to CyberCyan,
                        "Green" to CyberNeonGreen,
                        "Amber" to CyberAmber
                    )
                    colors.forEach { (name, color) ->
                        val isSelected = hudConfig.accentColorArgb == color.toArgb()
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setHudAccentColor(color.toArgb()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opacity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("HUD Background Opacity", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(hudConfig.opacity * 100).toInt()}%", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = hudConfig.opacity,
                    onValueChange = { viewModel.setHudOpacity(it) },
                    valueRange = 0.3f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(hudConfig.accentColorArgb),
                        activeTrackColor = Color(hudConfig.accentColorArgb),
                        inactiveTrackColor = CarbonDark
                    )
                )
            }
        }

        // Metric Toggles Matrix
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = CyberNeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DISPLAYED TELEMETRY METRICS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                MetricToggleItem(
                    title = "Real Frame Rate (FPS)",
                    subtitle = "Accurate rolling frame render interval",
                    checked = hudConfig.showFps,
                    onCheckedChange = { viewModel.toggleMetric("fps", it) }
                )
                MetricToggleItem(
                    title = "AMOLED Refresh Rate (Hz)",
                    subtitle = "Current display hardware frequency",
                    checked = hudConfig.showRefreshRate,
                    onCheckedChange = { viewModel.toggleMetric("refresh", it) }
                )
                MetricToggleItem(
                    title = "Battery Temperature (°C)",
                    subtitle = "Real-time battery thermal sensor reading",
                    checked = hudConfig.showBatteryTemp,
                    onCheckedChange = { viewModel.toggleMetric("temp", it) }
                )
                MetricToggleItem(
                    title = "Battery Percentage (%)",
                    subtitle = "Current charge status & charging state",
                    checked = hudConfig.showBattery,
                    onCheckedChange = { viewModel.toggleMetric("battery", it) }
                )
                MetricToggleItem(
                    title = "RAM Memory Usage (%)",
                    subtitle = "Available and used device RAM",
                    checked = hudConfig.showRam,
                    onCheckedChange = { viewModel.toggleMetric("ram", it) }
                )
                MetricToggleItem(
                    title = "Network Latency (Ping ms)",
                    subtitle = "Low-level socket ping measurement",
                    checked = hudConfig.showPing,
                    onCheckedChange = { viewModel.toggleMetric("ping", it) }
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CyberCrimson.copy(alpha = 0.2f) else CarbonDark)
            .border(
                1.dp,
                if (isSelected) CyberCrimson else CarbonBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                color = if (isSelected) CyberCrimson else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun MetricToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyberCrimson,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CarbonDark
            )
        )
    }
}
