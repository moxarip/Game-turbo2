package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun AudioToolsScreen(viewModel: GameCoreViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Audio Cockpit", "Session Logs")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonDark,
            contentColor = CyberCyan,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyberCyan,
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
            0 -> AudioContent()
            1 -> SessionHistoryScreen(viewModel = viewModel)
        }
    }
}

@Composable
private fun AudioContent() {
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    val maxMusicVol = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var currentMusicVol by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxMusicVol)
    }

    val maxNotifVol = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) }
    var currentNotifVol by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION).toFloat() / maxNotifVol)
    }

    var selectedProfile by remember { mutableStateOf("Footsteps Focus") }
    var showAudioDisclosure by remember { mutableStateOf(false) }

    fun playChannelTone(isLeftChannel: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 44100
                val durationSec = 0.8
                val numSamples = (durationSec * sampleRate).toInt()
                val freq = 440.0 // 440Hz A tone

                val buffer = ShortArray(numSamples * 2) // Stereo (L, R, L, R)
                for (i in 0 until numSamples) {
                    val sample = (sin(2.0 * Math.PI * i * freq / sampleRate) * Short.MAX_VALUE * 0.5).toInt().toShort()
                    if (isLeftChannel) {
                        buffer[i * 2] = sample      // Left
                        buffer[i * 2 + 1] = 0        // Right mute
                    } else {
                        buffer[i * 2] = 0            // Left mute
                        buffer[i * 2 + 1] = sample   // Right
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
            } catch (_: Exception) {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUDIO OPTIMIZATION & TEST",
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tactical Sound Cockpit",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }

                IconButton(onClick = { showAudioDisclosure = true }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = CyberCyan)
                }
            }
        }

        // Stereo Directionality Tester (Left & Right channel check)
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
                    Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EARPHONE SPATIAL DIRECTION TEST",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Play stereo positioning test tones to verify your left and right earbuds before matching in PUBG.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { playChannelTone(isLeftChannel = true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEST LEFT EAR", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { playChannelTone(isLeftChannel = false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyberCrimson, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEST RIGHT EAR", color = CyberCrimson, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Volume Sliders
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "DEVICE VOLUME STREAMS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Media
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Game Media Volume", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(currentMusicVol * 100).toInt()}%", color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Slider(
                    value = currentMusicVol,
                    onValueChange = { newVol ->
                        currentMusicVol = newVol
                        val targetInt = (newVol * maxMusicVol).toInt()
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetInt, 0)
                        } catch (_: Exception) {}
                    },
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan, inactiveTrackColor = CarbonDark)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notifications
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Notifications Volume", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(currentNotifVol * 100).toInt()}%", color = CyberAmber, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Slider(
                    value = currentNotifVol,
                    onValueChange = { newVol ->
                        currentNotifVol = newVol
                        val targetInt = (newVol * maxNotifVol).toInt()
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, targetInt, 0)
                        } catch (_: Exception) {}
                    },
                    colors = SliderDefaults.colors(thumbColor = CyberAmber, activeTrackColor = CyberAmber, inactiveTrackColor = CarbonDark)
                )
            }
        }

        // Tactical EQ Profiles Guidance
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
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = CyberNeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TACTICAL AUDIO PROFILES",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    "Footsteps Focus" to "Enhances footsteps audio frequencies (2.5 kHz to 4.5 kHz range) for early detection of creeping players.",
                    "Bass Explosions" to "Boosts deep sub-bass (60 Hz to 120 Hz) for high-impact vehicular combat and grenade immersion.",
                    "Balanced Gaming" to "Linear frequency response preserving original developer soundstage with clean voice comms.",
                    "Night Stealth" to "Dynamic peak limiter profile preventing sudden loud deafening gunshots while gaming in quiet environments."
                ).forEach { (profileName, desc) ->
                    val isSelected = selectedProfile == profileName
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.15f) else CarbonDark)
                            .border(1.dp, if (isSelected) CyberCyan else CarbonBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedProfile = profileName }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = profileName, color = if (isSelected) CyberCyan else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (isSelected) {
                                    Text("Active", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = desc, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAudioDisclosure) {
        AlertDialog(
            onDismissRequest = { showAudioDisclosure = false },
            title = { Text("Audio Tools Information", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Android security isolates audio streams between applications. GameCore Pro manages system output channels, provides hardware volume routing, earphone stereo orientation tests, and recommended frequency guides without illegally tampering with other apps' private audio buffers.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAudioDisclosure = false }) {
                    Text("OK", color = CyberCyan)
                }
            },
            containerColor = CarbonDark
        )
    }
}
