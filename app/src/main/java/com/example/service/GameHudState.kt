package com.example.service

import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.CyberCrimson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HudMode {
    MINI,       // Compact bar with FPS + Temp + Battery
    GAMING,     // Balanced HUD with FPS, Refresh, Battery, RAM, Ping
    ADVANCED    // Full telemetric HUD with CPU, GPU Thermal, Memory Graph, Ping RTT
}

enum class CrosshairStyle {
    DOT,
    CIRCLE,
    CROSS,
    CIRCLE_DOT,
    T_TACTICAL,
    CHEVRON
}

data class VisionFilterConfig(
    val isEnabled: Boolean = false,
    val presetName: String = "Natural",
    val invertColors: Boolean = false,
    val contrastLevel: Float = 1.0f,      // 1.0f = normal, up to 2.5f
    val saturationLevel: Float = 1.0f,    // 0.0f = b/w, 1.0f = normal, 2.0f = hyper-vibrant
    val darkVisionBoost: Float = 0.0f,    // 0.0f = none, up to 1.0f
    val sharpnessSimulation: Boolean = false
)

data class CrosshairConfig(
    val isEnabled: Boolean = false,
    val style: CrosshairStyle = CrosshairStyle.CROSS,
    val colorArgb: Int = CyberCrimson.toArgb(),
    val sizeDp: Int = 32,
    val thicknessDp: Int = 2,
    val opacity: Float = 0.95f,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val centerGapDp: Int = 4
)

data class HudDisplayConfig(
    val mode: HudMode = HudMode.GAMING,
    val scale: Float = 1.0f,
    val opacity: Float = 0.92f,
    val accentColorArgb: Int = CyberCrimson.toArgb(),
    val showFps: Boolean = true,
    val showRefreshRate: Boolean = true,
    val showBattery: Boolean = true,
    val showBatteryTemp: Boolean = true,
    val showCpuThermal: Boolean = true,
    val showRam: Boolean = true,
    val showPing: Boolean = true
)

object GameHudManager {
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(false)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _hudConfig = MutableStateFlow(HudDisplayConfig())
    val hudConfig: StateFlow<HudDisplayConfig> = _hudConfig.asStateFlow()

    private val _visionConfig = MutableStateFlow(VisionFilterConfig())
    val visionConfig: StateFlow<VisionFilterConfig> = _visionConfig.asStateFlow()

    private val _crosshairConfig = MutableStateFlow(CrosshairConfig())
    val crosshairConfig: StateFlow<CrosshairConfig> = _crosshairConfig.asStateFlow()

    private val _activeGameTitle = MutableStateFlow("PUBG Mobile")
    val activeGameTitle: StateFlow<String> = _activeGameTitle.asStateFlow()

    private val _sessionStartTimeMs = MutableStateFlow(0L)
    val sessionStartTimeMs: StateFlow<Long> = _sessionStartTimeMs.asStateFlow()

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
        if (running && _sessionStartTimeMs.value == 0L) {
            _sessionStartTimeMs.value = System.currentTimeMillis()
        } else if (!running) {
            _sessionStartTimeMs.value = 0L
            _isSidebarOpen.value = false
        }
    }

    fun setSidebarOpen(open: Boolean) {
        _isSidebarOpen.value = open
    }

    fun toggleSidebar() {
        _isSidebarOpen.value = !_isSidebarOpen.value
    }

    fun updateHudConfig(update: (HudDisplayConfig) -> HudDisplayConfig) {
        _hudConfig.value = update(_hudConfig.value)
    }

    fun updateVisionConfig(update: (VisionFilterConfig) -> VisionFilterConfig) {
        _visionConfig.value = update(_visionConfig.value)
    }

    fun updateCrosshairConfig(update: (CrosshairConfig) -> CrosshairConfig) {
        _crosshairConfig.value = update(_crosshairConfig.value)
    }

    fun setActiveGame(title: String) {
        _activeGameTitle.value = title
    }

    fun resetVisionFilter() {
        _visionConfig.value = VisionFilterConfig(
            isEnabled = false,
            presetName = "Natural",
            invertColors = false,
            contrastLevel = 1.0f,
            saturationLevel = 1.0f,
            darkVisionBoost = 0.0f
        )
    }
}
