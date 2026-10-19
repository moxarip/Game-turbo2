package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.CrosshairStyle
import com.example.service.ui.FloatingCrosshairView
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
import kotlinx.coroutines.delay
import kotlin.random.Random

data class TouchPointer(
    val id: Long,
    val x: Float,
    val y: Float
)

@Composable
fun AimToolsScreen(viewModel: GameCoreViewModel) {
    val context = LocalContext.current
    val crosshairConfig by viewModel.crosshairConfig.collectAsState()
    val recentAimScores by viewModel.recentAimScores.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Reticle Studio", "Aim Practice", "Touch Tester")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonDark,
            contentColor = CyberCrimson,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyberCrimson,
                    height = 2.5.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (selectedTab == index) Color.White else TextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ReticleStudioTab(viewModel)
            1 -> AimPracticeTab(viewModel, context)
            2 -> TouchTesterTab()
        }
    }
}

@Composable
private fun ReticleStudioTab(viewModel: GameCoreViewModel) {
    val crosshairConfig by viewModel.crosshairConfig.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reticle Target Sandbox Preview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D0F14))
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Tactical Target Circles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension * 0.42f
                    // Concentric target rings
                    drawCircle(Color.White.copy(alpha = 0.05f), maxR, c)
                    drawCircle(Color.White.copy(alpha = 0.08f), maxR * 0.7f, c)
                    drawCircle(Color.White.copy(alpha = 0.12f), maxR * 0.4f, c)
                    drawCircle(Color.White.copy(alpha = 0.18f), maxR * 0.15f, c)
                    // Crosshair background guides
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, c.y), Offset(size.width, c.y))
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(c.x, 0f), Offset(c.x, size.height))
                }

                // The Actual Floating Crosshair View
                FloatingCrosshairView(config = viewModel.crosshairConfig)

                Text(
                    text = "TARGET RANGE CALIBRATION",
                    color = TextMuted.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }
        }

        // Master Switch
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CarbonCard)
                    .border(1.dp, if (crosshairConfig.isEnabled) CyberCrimson else CarbonBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating Crosshair Overlay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Overlays centered reticle over PUBG with zero touch lag", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = crosshairConfig.isEnabled,
                        onCheckedChange = { viewModel.toggleCrosshair(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCrimson
                        )
                    )
                }
            }
        }

        // Reticle Style Picker
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
                    text = "RETICLE SHAPE STYLE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Cross" to CrosshairStyle.CROSS,
                        "Dot" to CrosshairStyle.DOT,
                        "Circle" to CrosshairStyle.CIRCLE,
                        "Tactical" to CrosshairStyle.CIRCLE_DOT,
                        "T-Cross" to CrosshairStyle.T_TACTICAL,
                        "Chevron" to CrosshairStyle.CHEVRON
                    ).forEach { (name, style) ->
                        val isSelected = crosshairConfig.style == style
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberCrimson.copy(alpha = 0.25f) else CarbonDark)
                                .border(1.dp, if (isSelected) CyberCrimson else CarbonBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setCrosshairStyle(style) }
                                .padding(vertical = 8.dp),
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
                }
            }
        }

        // Color & Appearance Sliders
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
                    text = "RETICLE CUSTOMIZATION",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Color picker
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val colors = listOf(
                        CyberCrimson,
                        CyberNeonGreen,
                        CyberCyan,
                        CyberAmber,
                        Color.White,
                        Color(0xFFE040FB)
                    )
                    colors.forEach { col ->
                        val isSelected = crosshairConfig.colorArgb == col.toArgb()
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                                .clickable { viewModel.setCrosshairColor(col.toArgb()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Size
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Reticle Size", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${crosshairConfig.sizeDp} dp", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Slider(
                    value = crosshairConfig.sizeDp.toFloat(),
                    onValueChange = { viewModel.setCrosshairSize(it.toInt()) },
                    valueRange = 16f..64f,
                    colors = SliderDefaults.colors(thumbColor = CyberCrimson, activeTrackColor = CyberCrimson, inactiveTrackColor = CarbonDark)
                )

                // Thickness
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Line Thickness", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${crosshairConfig.thicknessDp} dp", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Slider(
                    value = crosshairConfig.thicknessDp.toFloat(),
                    onValueChange = { viewModel.setCrosshairThickness(it.toInt()) },
                    valueRange = 1f..6f,
                    colors = SliderDefaults.colors(thumbColor = CyberCrimson, activeTrackColor = CyberCrimson, inactiveTrackColor = CarbonDark)
                )

                // Center Gap
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Center Clear Gap", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${crosshairConfig.centerGapDp} dp", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Slider(
                    value = crosshairConfig.centerGapDp.toFloat(),
                    onValueChange = { viewModel.setCrosshairGap(it.toInt()) },
                    valueRange = 0f..16f,
                    colors = SliderDefaults.colors(thumbColor = CyberCrimson, activeTrackColor = CyberCrimson, inactiveTrackColor = CarbonDark)
                )
            }
        }

        // Fine Position Calibration (Nudge D-Pad)
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
                    Column {
                        Text(
                            text = "POSITION CALIBRATION",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Nudge reticle to align perfectly with PUBG hipfire center",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "X:${crosshairConfig.offsetX} Y:${crosshairConfig.offsetY}",
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // D-Pad
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { viewModel.nudgeCrosshair(0, -2) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(CarbonDark, RoundedCornerShape(6.dp))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.nudgeCrosshair(-2, 0) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(CarbonDark, RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { viewModel.resetCrosshairPosition() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(CyberCrimson.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.CenterFocusStrong, contentDescription = "Center", tint = CyberCrimson, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { viewModel.nudgeCrosshair(2, 0) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(CarbonDark, RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(
                        onClick = { viewModel.nudgeCrosshair(0, 2) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(CarbonDark, RoundedCornerShape(6.dp))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Interactive Aim Practice Mini-Game Drill
 */
@Composable
private fun AimPracticeTab(viewModel: GameCoreViewModel, context: Context) {
    var isDrillRunning by remember { mutableStateOf(false) }
    var timeLeftSeconds by remember { mutableIntStateOf(20) }
    var currentScore by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var targetSpawnTimeMs by remember { mutableLongStateOf(0L) }
    var totalReactionTimeMs by remember { mutableLongStateOf(0L) }

    // Target position in fractional coordinates (0.1f to 0.9f)
    var targetX by remember { mutableFloatStateOf(0.5f) }
    var targetY by remember { mutableFloatStateOf(0.5f) }
    var targetRadiusDp by remember { mutableFloatStateOf(26f) }

    val recentScores by viewModel.recentAimScores.collectAsState()

    // Vibration helper
    val triggerVibe = remember {
        {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(30)
                }
            } catch (_: Exception) {}
        }
    }

    // Timer loop
    LaunchedEffect(isDrillRunning) {
        if (isDrillRunning) {
            timeLeftSeconds = 20
            currentScore = 0
            hits = 0
            misses = 0
            totalReactionTimeMs = 0L
            targetSpawnTimeMs = System.currentTimeMillis()
            targetX = Random.nextFloat() * 0.7f + 0.15f
            targetY = Random.nextFloat() * 0.7f + 0.15f

            while (timeLeftSeconds > 0 && isDrillRunning) {
                delay(1000)
                timeLeftSeconds--
            }

            if (isDrillRunning) {
                isDrillRunning = false
                val accuracy = if (hits + misses > 0) (hits.toFloat() / (hits + misses)) * 100f else 0f
                val avgReaction = if (hits > 0) (totalReactionTimeMs / hits).toInt() else 0
                viewModel.recordAimDrillResult(
                    score = currentScore,
                    hits = hits,
                    misses = misses,
                    accuracy = accuracy,
                    reactionTimeMs = avgReaction
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Drill Stats Banner
        item {
            Box(
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
                    AimStat(label = "TIME", value = "${timeLeftSeconds}s", color = if (timeLeftSeconds <= 5) CyberCrimson else Color.White)
                    AimStat(label = "SCORE", value = "$currentScore", color = CyberCyan)
                    AimStat(label = "HITS", value = "$hits", color = CyberNeonGreen)
                    val accuracy = if (hits + misses > 0) ((hits.toFloat() / (hits + misses)) * 100).toInt() else 100
                    AimStat(label = "ACCURACY", value = "$accuracy%", color = CyberAmber)
                }
            }
        }

        // Aim Shooting Range Canvas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0B0D12))
                    .border(1.2.dp, if (isDrillRunning) CyberCrimson else CarbonBorder, RoundedCornerShape(16.dp))
                    .pointerInput(isDrillRunning) {
                        detectTapGestures { tapOffset ->
                            if (!isDrillRunning) return@detectTapGestures

                            val targetPxX = size.width * targetX
                            val targetPxY = size.height * targetY
                            val radiusPx = targetRadiusDp * 2.5f

                            val dist = Math.hypot((tapOffset.x - targetPxX).toDouble(), (tapOffset.y - targetPxY).toDouble()).toFloat()
                            if (dist <= radiusPx) {
                                // Hit!
                                triggerVibe()
                                val reaction = System.currentTimeMillis() - targetSpawnTimeMs
                                totalReactionTimeMs += reaction
                                val points = (1000 - reaction.coerceIn(100, 1000)).toInt() / 10
                                currentScore += points.coerceAtLeast(10)
                                hits++

                                // Spawn new target
                                targetSpawnTimeMs = System.currentTimeMillis()
                                targetX = Random.nextFloat() * 0.7f + 0.15f
                                targetY = Random.nextFloat() * 0.7f + 0.15f
                                targetRadiusDp = Random.nextInt(20, 32).toFloat()
                            } else {
                                misses++
                                currentScore = (currentScore - 15).coerceAtLeast(0)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isDrillRunning) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width * targetX
                        val centerY = size.height * targetY
                        val radius = targetRadiusDp * 2.2f

                        // Outer tactical ring
                        drawCircle(
                            color = CyberCrimson.copy(alpha = 0.35f),
                            radius = radius * 1.3f,
                            center = Offset(centerX, centerY)
                        )
                        // Target ring
                        drawCircle(
                            color = CyberCrimson,
                            radius = radius,
                            center = Offset(centerX, centerY)
                        )
                        // Inner ring
                        drawCircle(
                            color = Color.White,
                            radius = radius * 0.5f,
                            center = Offset(centerX, centerY)
                        )
                        // Bullseye dot
                        drawCircle(
                            color = CyberAmber,
                            radius = radius * 0.2f,
                            center = Offset(centerX, centerY)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.TrackChanges, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("PUBG REFLEX DRILL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap 'Start Drill' to practice flick shot reaction time", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Start / Stop Drill Action Button
        item {
            Button(
                onClick = { isDrillRunning = !isDrillRunning },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDrillRunning) CarbonDark else CyberCrimson
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isDrillRunning) androidx.compose.foundation.BorderStroke(1.dp, CyberCrimson) else null
            ) {
                Icon(
                    imageVector = if (isDrillRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isDrillRunning) CyberCrimson else Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDrillRunning) "ABORT DRILL" else "START 20s AIM DRILL",
                    color = if (isDrillRunning) CyberCrimson else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Local Scores History
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DRILL RECORDS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (recentScores.isEmpty()) {
                    Text("No drill sessions recorded yet. Start training above!", color = TextMuted, fontSize = 11.sp)
                } else {
                    recentScores.take(5).forEach { score ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score: ${score.score}",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Acc: ${score.accuracyPercentage.toInt()}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Avg: ${score.avgReactionTimeMs}ms",
                                color = CyberAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Touch Latency & Multi-Touch Tester Tab
 */
@Composable
private fun TouchTesterTab() {
    val pointers = remember { mutableStateListOf<TouchPointer>() }
    var touchEventCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "TOUCH SAMPLING & MULTI-TOUCH TESTER",
                color = CyberCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tests display multi-finger touch responsiveness for claw grips in PUBG.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Active Pointers Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonCard)
                .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("POINTER COUNT", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("${pointers.size} fingers", color = if (pointers.size >= 4) CyberNeonGreen else CyberCyan, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("SAMPLE EVENTS", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("$touchEventCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("TOUCH SAMPLING", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("480Hz Native", color = CyberAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Touch Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF090B0E))
                .border(1.2.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            pointers.clear()
                            touchEventCount++
                            for (change in event.changes) {
                                if (change.pressed) {
                                    pointers.add(TouchPointer(change.id.value, change.position.x, change.position.y))
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw coordinate guides
                for (p in pointers) {
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.25f),
                        radius = 65f,
                        center = Offset(p.x, p.y)
                    )
                    drawCircle(
                        color = CyberCyan,
                        radius = 28f,
                        center = Offset(p.x, p.y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(p.x, p.y)
                    )
                    // Crosshair on finger
                    drawLine(CyberCyan, Offset(p.x - 40f, p.y), Offset(p.x + 40f, p.y), strokeWidth = 2f)
                    drawLine(CyberCyan, Offset(p.x, p.y - 40f), Offset(p.x, p.y + 40f), strokeWidth = 2f)
                }
            }

            if (pointers.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = CyberCyan.copy(alpha = 0.4f), modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Touch with 2, 3, or 4 fingers to test claw responsiveness", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AimStat(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = value, color = color, fontWeight = FontWeight.Black, fontSize = 17.sp, fontFamily = FontFamily.Monospace)
    }
}
