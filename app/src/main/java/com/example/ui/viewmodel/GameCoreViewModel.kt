package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GameCoreApplication
import com.example.data.model.AimScoreEntity
import com.example.data.model.GameProfileEntity
import com.example.data.model.GameSessionEntity
import com.example.data.model.VisionPresetEntity
import com.example.service.CrosshairConfig
import com.example.service.CrosshairStyle
import com.example.service.GameHudManager
import com.example.service.GameHudService
import com.example.service.HudDisplayConfig
import com.example.service.HudMode
import com.example.service.VisionFilterConfig
import com.example.telemetry.HardwareTelemetry
import com.example.telemetry.TelemetryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isGame: Boolean
)

class GameCoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as GameCoreApplication).repository
    private val telemetry = HardwareTelemetry(application).apply { start() }

    val telemetryState: StateFlow<TelemetryState> = telemetry.telemetry
    val isHudRunning: StateFlow<Boolean> = GameHudManager.isServiceRunning
    val hudConfig: StateFlow<HudDisplayConfig> = GameHudManager.hudConfig
    val visionConfig: StateFlow<VisionFilterConfig> = GameHudManager.visionConfig
    val crosshairConfig: StateFlow<CrosshairConfig> = GameHudManager.crosshairConfig

    val gameProfiles: StateFlow<List<GameProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visionPresets: StateFlow<List<VisionPresetEntity>> = repository.allVisionPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameSessions: StateFlow<List<GameSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAimScores: StateFlow<List<AimScoreEntity>> = repository.recentAimScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _selectedGameProfile = MutableStateFlow<GameProfileEntity?>(null)
    val selectedGameProfile: StateFlow<GameProfileEntity?> = _selectedGameProfile.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    init {
        checkOverlayPermission()
        scanInstalledGames()
    }

    fun checkOverlayPermission() {
        val context = getApplication<Application>()
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)
    }

    fun scanInstalledGames() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val list = mutableListOf<InstalledAppItem>()

            for (appInfo in packages) {
                // Filter out system apps without launcher intents
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                                appInfo.packageName.contains("pubg", ignoreCase = true) ||
                                appInfo.packageName.contains("game", ignoreCase = true) ||
                                appInfo.packageName.contains("cod", ignoreCase = true)
                    } else {
                        appInfo.packageName.contains("game", ignoreCase = true)
                    }
                    val label = pm.getApplicationLabel(appInfo).toString()
                    list.add(InstalledAppItem(appInfo.packageName, label, isGame))
                }
            }
            _installedApps.value = list.sortedWith(compareByDescending<InstalledAppItem> { it.isGame }.thenBy { it.appName })
        }
    }

    fun toggleHudService() {
        val context = getApplication<Application>()
        if (GameHudManager.isServiceRunning.value) {
            val stopIntent = Intent(context, GameHudService::class.java).apply {
                action = GameHudService.ACTION_STOP_HUD
            }
            context.startService(stopIntent)
        } else {
            if (Settings.canDrawOverlays(context)) {
                val startIntent = Intent(context, GameHudService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(startIntent)
                } else {
                    context.startService(startIntent)
                }
            }
        }
    }

    fun launchGame(packageName: String, gameTitle: String) {
        val context = getApplication<Application>()
        // Automatically start HUD if permission is granted
        if (Settings.canDrawOverlays(context) && !GameHudManager.isServiceRunning.value) {
            val startIntent = Intent(context, GameHudService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
        }
        GameHudManager.setActiveGame(gameTitle)

        // Launch app
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun selectProfile(profile: GameProfileEntity?) {
        _selectedGameProfile.value = profile
    }

    fun saveGameProfile(profile: GameProfileEntity) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun deleteGameProfile(profile: GameProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    // HUD Config
    fun setHudMode(mode: HudMode) {
        GameHudManager.updateHudConfig { it.copy(mode = mode) }
    }

    fun setHudScale(scale: Float) {
        GameHudManager.updateHudConfig { it.copy(scale = scale) }
    }

    fun setHudOpacity(opacity: Float) {
        GameHudManager.updateHudConfig { it.copy(opacity = opacity) }
    }

    fun setHudAccentColor(colorArgb: Int) {
        GameHudManager.updateHudConfig { it.copy(accentColorArgb = colorArgb) }
    }

    fun toggleMetric(metric: String, enabled: Boolean) {
        GameHudManager.updateHudConfig { cfg ->
            when (metric) {
                "fps" -> cfg.copy(showFps = enabled)
                "refresh" -> cfg.copy(showRefreshRate = enabled)
                "battery" -> cfg.copy(showBattery = enabled)
                "temp" -> cfg.copy(showBatteryTemp = enabled)
                "thermal" -> cfg.copy(showCpuThermal = enabled)
                "ram" -> cfg.copy(showRam = enabled)
                "ping" -> cfg.copy(showPing = enabled)
                else -> cfg
            }
        }
    }

    // Hunt Vision
    fun applyVisionPreset(preset: VisionPresetEntity) {
        GameHudManager.updateVisionConfig {
            it.copy(
                isEnabled = preset.name != "Natural",
                presetName = preset.name,
                invertColors = preset.invertColors,
                contrastLevel = preset.contrastLevel,
                saturationLevel = preset.saturationLevel,
                darkVisionBoost = preset.darkVisionBoost,
                sharpnessSimulation = preset.sharpnessSimulation
            )
        }
    }

    fun toggleVisionEnabled(enabled: Boolean) {
        GameHudManager.updateVisionConfig { it.copy(isEnabled = enabled) }
    }

    fun updateVisionParams(
        invert: Boolean,
        contrast: Float,
        saturation: Float,
        darkVision: Float
    ) {
        GameHudManager.updateVisionConfig {
            it.copy(
                isEnabled = true,
                invertColors = invert,
                contrastLevel = contrast,
                saturationLevel = saturation,
                darkVisionBoost = darkVision
            )
        }
    }

    fun saveCustomVisionPreset(name: String) {
        val current = visionConfig.value
        val entity = VisionPresetEntity(
            name = name,
            isBuiltIn = false,
            invertColors = current.invertColors,
            contrastLevel = current.contrastLevel,
            saturationLevel = current.saturationLevel,
            darkVisionBoost = current.darkVisionBoost,
            sharpnessSimulation = current.sharpnessSimulation
        )
        viewModelScope.launch {
            repository.savePreset(entity)
        }
    }

    fun deleteVisionPreset(preset: VisionPresetEntity) {
        viewModelScope.launch {
            if (!preset.isBuiltIn) {
                repository.deletePreset(preset)
            }
        }
    }

    fun resetVisionFilter() {
        GameHudManager.resetVisionFilter()
    }

    // Aim Tools
    fun toggleCrosshair(enabled: Boolean) {
        GameHudManager.updateCrosshairConfig { it.copy(isEnabled = enabled) }
    }

    fun setCrosshairStyle(style: CrosshairStyle) {
        GameHudManager.updateCrosshairConfig { it.copy(style = style) }
    }

    fun setCrosshairColor(colorArgb: Int) {
        GameHudManager.updateCrosshairConfig { it.copy(colorArgb = colorArgb) }
    }

    fun setCrosshairSize(sizeDp: Int) {
        GameHudManager.updateCrosshairConfig { it.copy(sizeDp = sizeDp) }
    }

    fun setCrosshairThickness(thicknessDp: Int) {
        GameHudManager.updateCrosshairConfig { it.copy(thicknessDp = thicknessDp) }
    }

    fun setCrosshairOpacity(opacity: Float) {
        GameHudManager.updateCrosshairConfig { it.copy(opacity = opacity) }
    }

    fun setCrosshairGap(gapDp: Int) {
        GameHudManager.updateCrosshairConfig { it.copy(centerGapDp = gapDp) }
    }

    fun nudgeCrosshair(deltaX: Int, deltaY: Int) {
        GameHudManager.updateCrosshairConfig {
            it.copy(offsetX = it.offsetX + deltaX, offsetY = it.offsetY + deltaY)
        }
    }

    fun resetCrosshairPosition() {
        GameHudManager.updateCrosshairConfig {
            it.copy(offsetX = 0, offsetY = 0)
        }
    }

    // Aim Practice Score Logging
    fun recordAimDrillResult(
        score: Int,
        hits: Int,
        misses: Int,
        accuracy: Float,
        reactionTimeMs: Int
    ) {
        viewModelScope.launch {
            repository.recordAimScore(
                AimScoreEntity(
                    score = score,
                    hits = hits,
                    misses = misses,
                    accuracyPercentage = accuracy,
                    avgReactionTimeMs = reactionTimeMs
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        telemetry.stop()
    }
}
