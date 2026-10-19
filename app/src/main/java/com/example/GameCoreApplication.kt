package com.example

import android.app.Application
import com.example.data.local.GameCoreDatabase
import com.example.data.repository.GameCoreRepository

class GameCoreApplication : Application() {
    val database: GameCoreDatabase by lazy { GameCoreDatabase.getDatabase(this) }
    val repository: GameCoreRepository by lazy {
        GameCoreRepository(
            database.gameProfileDao(),
            database.gameSessionDao(),
            database.visionPresetDao(),
            database.aimScoreDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: GameCoreApplication
            private set
    }
}
