package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GameCoreDatabase
import com.example.data.model.AimScoreEntity
import com.example.data.model.GameProfileEntity
import com.example.data.model.GameSessionEntity
import com.example.data.model.VisionPresetEntity
import com.example.service.CrosshairStyle
import com.example.service.GameHudManager
import com.example.service.HudMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: GameCoreDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GameCoreDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GameCore Pro", appName)
    }

    @Test
    fun testGameProfileDaoInsertAndQuery() = runBlocking {
        val dao = database.gameProfileDao()
        val profile = GameProfileEntity(
            packageName = "com.tencent.ig",
            appName = "PUBG Mobile",
            hudMode = "GAMING",
            crosshairStyle = "CIRCLE_DOT",
            crosshairColor = -65536,
            touchNotes = "Camera 110%"
        )
        val id = dao.insertProfile(profile)
        assertTrue(id > 0)

        val retrieved = dao.getProfileByPackage("com.tencent.ig")
        assertNotNull(retrieved)
        assertEquals("PUBG Mobile", retrieved?.appName)
        assertEquals("CIRCLE_DOT", retrieved?.crosshairStyle)
    }

    @Test
    fun testVisionPresetsDao() = runBlocking {
        val dao = database.visionPresetDao()
        val darkVision = VisionPresetEntity(
            name = "Dark Vision Test",
            isBuiltIn = true,
            invertColors = false,
            contrastLevel = 1.4f,
            darkVisionBoost = 0.8f
        )
        val id = dao.insertPreset(darkVision)
        assertTrue(id > 0)

        val retrieved = dao.getPresetById(id)
        assertNotNull(retrieved)
        assertEquals(0.8f, retrieved?.darkVisionBoost)
    }

    @Test
    fun testAimScoreDao() = runBlocking {
        val dao = database.aimScoreDao()
        dao.insertScore(
            AimScoreEntity(
                score = 850,
                hits = 15,
                misses = 2,
                accuracyPercentage = 88.2f,
                avgReactionTimeMs = 240
            )
        )

        val highScore = dao.getHighScore()
        assertEquals(850, highScore)

        val recent = dao.getRecentScores().first()
        assertEquals(1, recent.size)
        assertEquals(850, recent[0].score)
    }

    @Test
    fun testGameHudManagerToggles() {
        GameHudManager.setServiceRunning(true)
        assertTrue(GameHudManager.isServiceRunning.value)

        GameHudManager.updateHudConfig { it.copy(mode = HudMode.MINI) }
        assertEquals(HudMode.MINI, GameHudManager.hudConfig.value.mode)

        GameHudManager.updateCrosshairConfig { it.copy(isEnabled = true, style = CrosshairStyle.CHEVRON) }
        assertTrue(GameHudManager.crosshairConfig.value.isEnabled)
        assertEquals(CrosshairStyle.CHEVRON, GameHudManager.crosshairConfig.value.style)

        GameHudManager.resetVisionFilter()
        assertFalse(GameHudManager.visionConfig.value.isEnabled)
        assertEquals("Natural", GameHudManager.visionConfig.value.presetName)

        GameHudManager.setServiceRunning(false)
        assertFalse(GameHudManager.isServiceRunning.value)
    }
}
