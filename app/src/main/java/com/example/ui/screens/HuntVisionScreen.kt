package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VisionPresetEntity
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
fun HuntVisionScreen(viewModel: GameCoreViewModel) {
    val visionConfig by viewModel.visionConfig.collectAsState()
    val visionPresets by viewModel.visionPresets.collectAsState()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HUNT VISION FILTERS",
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tactical Screen Enhancement",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }

                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = CyberCyan)
                }
            }
        }

        // Live Interactive Sandbox Preview
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SIMULATED GAME SCENE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (visionConfig.isEnabled) "ACTIVE: ${visionConfig.presetName}" else "FILTER DISABLED",
                        color = if (visionConfig.isEnabled) CyberNeonGreen else TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas scene showing a tactical PUBG-style environment (building, dark shadow, sky, target)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Sky & terrain background
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF263238), Color(0xFF1B2428), Color(0xFF0F1416))
                            )
                        )

                        // Dark building shadow zone (for testing Dark Vision gamma lift!)
                        drawRect(
                            color = Color(0xFF0A0D10),
                            topLeft = Offset(0f, size.height * 0.35f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.45f, size.height * 0.65f)
                        )

                        // Window in dark building with enemy silhouette
                        drawRect(
                            color = Color(0xFF161A20),
                            topLeft = Offset(size.width * 0.12f, size.height * 0.45f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.18f, size.height * 0.28f)
                        )
                        // Silhouette inside window
                        drawCircle(
                            color = Color(0xFF050708),
                            radius = 16f,
                            center = Offset(size.width * 0.21f, size.height * 0.52f)
                        )

                        // Open bright area
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF455A64), Color(0xFF37474F))
                            ),
                            topLeft = Offset(size.width * 0.45f, size.height * 0.4f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.55f, size.height * 0.6f)
                        )

                        // Distant vehicle / obstacle
                        drawRect(
                            color = Color(0xFFD32F2F),
                            topLeft = Offset(size.width * 0.65f, size.height * 0.6f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.2f, size.height * 0.15f)
                        )

                        // Grid lines for edge sharpness evaluation
                        drawLine(
                            color = Color.White.copy(alpha = 0.2f),
                            start = Offset(0f, size.height * 0.35f),
                            end = Offset(size.width * 0.45f, size.height * 0.35f),
                            strokeWidth = 2f
                        )

                        // Now apply the live simulated filter overlay
                        if (visionConfig.isEnabled) {
                            if (visionConfig.invertColors) {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.92f),
                                    blendMode = BlendMode.Difference
                                )
                            } else if (visionConfig.darkVisionBoost > 0f) {
                                val boostAlpha = (visionConfig.darkVisionBoost * 0.4f).coerceIn(0.05f, 0.45f)
                                drawRect(
                                    color = Color(0xFF408080).copy(alpha = boostAlpha),
                                    blendMode = BlendMode.Screen
                                )
                            } else if (visionConfig.contrastLevel > 1.2f) {
                                val contrastAlpha = ((visionConfig.contrastLevel - 1.0f) * 0.35f).coerceIn(0.05f, 0.45f)
                                drawRect(
                                    color = Color(0xFF101015).copy(alpha = contrastAlpha),
                                    blendMode = BlendMode.Overlay
                                )
                            } else if (visionConfig.saturationLevel > 1.3f) {
                                val satAlpha = ((visionConfig.saturationLevel - 1.0f) * 0.2f).coerceIn(0.05f, 0.3f)
                                drawRect(
                                    color = Color(0xFFFF4081).copy(alpha = satAlpha),
                                    blendMode = BlendMode.Color
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = visionConfig.isEnabled,
                            onCheckedChange = { viewModel.toggleVisionEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberCyan
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (visionConfig.isEnabled) "Filter Active Over Games" else "Filter Disabled",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { viewModel.resetVisionFilter() },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore Original", color = CyberCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tactical Presets (Section 2)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRESETS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )

                    TextButton(onClick = { showSavePresetDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Current", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Presets list
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    visionPresets.forEach { preset ->
                        val isSelected = visionConfig.isEnabled && visionConfig.presetName == preset.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyberCyan.copy(alpha = 0.15f) else CarbonDark)
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else CarbonBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.applyVisionPreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.name,
                                        color = if (isSelected) CyberCyan else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (preset.isBuiltIn) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CarbonCard)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("Default", color = TextMuted, fontSize = 9.sp)
                                        }
                                    }
                                }
                                Text(
                                    text = when (preset.name) {
                                        "Dark Vision" -> "Lifts shadows & equalizes black levels"
                                        "Negative / Invert" -> "Inverts colors for target silhouette spotting"
                                        "High Contrast" -> "Crisp enemy outline separation"
                                        "Vibrant" -> "Intensified color saturation"
                                        "Night Gaming" -> "Comfortable low glare dark tones"
                                        else -> "Standard display rendering"
                                    },
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            if (!preset.isBuiltIn) {
                                IconButton(
                                    onClick = { viewModel.deleteVisionPreset(preset) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberCrimson, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Manual Tuning Sliders
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "MANUAL VISION TUNING",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Invert switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Negative / Color Inversion", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Full-screen inverse luminance", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = visionConfig.invertColors,
                        onCheckedChange = {
                            viewModel.updateVisionParams(
                                invert = it,
                                contrast = visionConfig.contrastLevel,
                                saturation = visionConfig.saturationLevel,
                                darkVision = visionConfig.darkVisionBoost
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dark Vision (Black Equalizer)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Vision Boost (Shadows)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(visionConfig.darkVisionBoost * 100).toInt()}%", color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = visionConfig.darkVisionBoost,
                    onValueChange = {
                        viewModel.updateVisionParams(
                            invert = false,
                            contrast = visionConfig.contrastLevel,
                            saturation = visionConfig.saturationLevel,
                            darkVision = it
                        )
                    },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan, inactiveTrackColor = CarbonDark)
                )

                // Contrast Boost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Contrast Boost", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(String.format("%.1fx", visionConfig.contrastLevel), color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = visionConfig.contrastLevel,
                    onValueChange = {
                        viewModel.updateVisionParams(
                            invert = false,
                            contrast = it,
                            saturation = visionConfig.saturationLevel,
                            darkVision = visionConfig.darkVisionBoost
                        )
                    },
                    valueRange = 1.0f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan, inactiveTrackColor = CarbonDark)
                )

                // Saturation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Color Saturation", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(String.format("%.1fx", visionConfig.saturationLevel), color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = visionConfig.saturationLevel,
                    onValueChange = {
                        viewModel.updateVisionParams(
                            invert = false,
                            contrast = visionConfig.contrastLevel,
                            saturation = it,
                            darkVision = visionConfig.darkVisionBoost
                        )
                    },
                    valueRange = 0.0f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan, inactiveTrackColor = CarbonDark)
                )
            }
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Custom Preset", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.saveCustomVisionPreset(newPresetName.trim())
                            newPresetName = ""
                            showSavePresetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CarbonDark
        )
    }

    // Technical Disclosure Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("How Hunt Vision Works", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Pure Android Overlay Implementation: Filters run via a transparent WindowManager overlay using hardware accelerated ColorMatrix and blend modes.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Zero Root & Zero Game Tampering: Does not read game memory, hook binaries, or violate game integrity policies.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Dark Vision: Uses a gamma curve boost to equalize deep blacks and highlight camouflaged enemies in shaded corners.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 100% Reversible: Tapping 'Restore Original' or closing the HUD immediately clears all overlays with zero lingering effects.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CarbonDark
        )
    }
}
