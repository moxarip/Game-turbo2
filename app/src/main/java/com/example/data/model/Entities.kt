package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val hudMode: String = "GAMING", // MINI, GAMING, ADVANCED
    val visionPresetId: Long = 1,
    val crosshairStyle: String = "CROSS", // DOT, CIRCLE, CROSS, CIRCLE_DOT, T_TACTICAL, CHEVRON
    val crosshairColor: Int = -65536, // Red
    val crosshairSize: Int = 32,
    val crosshairThickness: Int = 2,
    val crosshairOpacity: Float = 0.9f,
    val crosshairOffsetX: Int = 0,
    val crosshairOffsetY: Int = 0,
    val dndEnabled: Boolean = true,
    val touchNotes: String = "PUBG: Camera 110%, ADS 95%, Gyro 300%",
    val audioPreset: String = "BALANCED",
    val lastPlayed: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gamePackage: String,
    val gameTitle: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val batteryDrain: Int, // e.g. 8%
    val peakTempCelsius: Float,
    val averageFps: Int
)

@Entity(tableName = "vision_presets")
data class VisionPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isBuiltIn: Boolean = false,
    val invertColors: Boolean = false,
    val contrastLevel: Float = 1.0f, // 1.0f normal, up to 2.5f
    val saturationLevel: Float = 1.0f, // 0.0f mono, 1.0f normal, 2.0f vibrant
    val darkVisionBoost: Float = 0.0f, // 0.0f none, 0.5f medium, 1.0f max gamma lift
    val sharpnessSimulation: Boolean = false
)

@Entity(tableName = "aim_scores")
data class AimScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val drillType: String = "REFLEX_TARGET",
    val score: Int,
    val hits: Int,
    val misses: Int,
    val accuracyPercentage: Float,
    val avgReactionTimeMs: Int
)
