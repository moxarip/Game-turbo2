package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AimScoreEntity
import com.example.data.model.GameProfileEntity
import com.example.data.model.GameSessionEntity
import com.example.data.model.VisionPresetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GameProfileEntity::class,
        GameSessionEntity::class,
        VisionPresetEntity::class,
        AimScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameCoreDatabase : RoomDatabase() {
    abstract fun gameProfileDao(): GameProfileDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun visionPresetDao(): VisionPresetDao
    abstract fun aimScoreDao(): AimScoreDao

    companion object {
        @Volatile
        private var INSTANCE: GameCoreDatabase? = null

        fun getDatabase(context: Context): GameCoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameCoreDatabase::class.java,
                    "gamecore_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: GameCoreDatabase) {
                val visionDao = database.visionPresetDao()
                // Built-in presets required by Section 2:
                // Natural, Vibrant, High Contrast, Negative, Dark Vision, Night Gaming
                visionDao.insertPreset(
                    VisionPresetEntity(
                        id = 1,
                        name = "Natural",
                        isBuiltIn = true,
                        invertColors = false,
                        contrastLevel = 1.0f,
                        saturationLevel = 1.0f,
                        darkVisionBoost = 0.0f
                    )
                )
                visionDao.insertPreset(
                    VisionPresetEntity(
                        id = 2,
                        name = "Vibrant",
                        isBuiltIn = true,
                        invertColors = false,
                        contrastLevel = 1.25f,
                        saturationLevel = 1.8f,
                        darkVisionBoost = 0.1f
                    )
                )
                visionDao.insertPreset(
                    VisionPresetEntity(
                        id = 3,
                        name = "High Contrast",
                        isBuiltIn = true,
                        invertColors = false,
                        contrastLevel = 1.9f,
                        saturationLevel = 1.3f,
                        darkVisionBoost = 0.2f
                    )
                )
                visionDao.insertPreset(
                    VisionPresetEntity(
                        id = 4,
                        name = "Negative / Invert",
                        isBuiltIn = true,
                        invertColors = true,
                        contrastLevel = 1.2f,
                        saturationLevel = 1.0f,
                        darkVisionBoost = 0.0f
                    )
                )
                visionDao.insertPreset(
                    VisionPresetEntity(
                        id = 5,
                        name = "Dark Vision",
                        isBuiltIn = true,
                        invertColors = false,
                        contrastLevel = 1.4f,
                        saturationLevel = 1.1f,
                        darkVisionBoost = 0.75f,
                        sharpnessSimulation = true
                    )
                )
                visionDao.insertPreset(
                    VisionPresetEntity(
                        id = 6,
                        name = "Night Gaming",
                        isBuiltIn = true,
                        invertColors = false,
                        contrastLevel = 0.9f,
                        saturationLevel = 0.7f,
                        darkVisionBoost = 0.1f
                    )
                )

                // Initial popular mobile game profile: PUBG Mobile
                val profileDao = database.gameProfileDao()
                profileDao.insertProfile(
                    GameProfileEntity(
                        id = 1,
                        packageName = "com.tencent.ig",
                        appName = "PUBG Mobile",
                        hudMode = "GAMING",
                        visionPresetId = 5, // Dark vision
                        crosshairStyle = "CIRCLE_DOT",
                        crosshairColor = -65536, // Red
                        crosshairSize = 34,
                        crosshairThickness = 2,
                        crosshairOpacity = 0.95f,
                        dndEnabled = true,
                        touchNotes = "Camera: 110% | ADS: 95% | Gyro 3rd Person: 300% | 4x: 180%",
                        audioPreset = "FOOTSTEPS_FOCUS"
                    )
                )
            }
        }
    }
}
