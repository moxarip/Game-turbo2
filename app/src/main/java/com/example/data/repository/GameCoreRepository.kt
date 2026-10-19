package com.example.data.repository

import com.example.data.local.AimScoreDao
import com.example.data.local.GameProfileDao
import com.example.data.local.GameSessionDao
import com.example.data.local.VisionPresetDao
import com.example.data.model.AimScoreEntity
import com.example.data.model.GameProfileEntity
import com.example.data.model.GameSessionEntity
import com.example.data.model.VisionPresetEntity
import kotlinx.coroutines.flow.Flow

class GameCoreRepository(
    private val profileDao: GameProfileDao,
    private val sessionDao: GameSessionDao,
    private val visionDao: VisionPresetDao,
    private val aimDao: AimScoreDao
) {
    val allProfiles: Flow<List<GameProfileEntity>> = profileDao.getAllProfiles()
    val allSessions: Flow<List<GameSessionEntity>> = sessionDao.getAllSessions()
    val allVisionPresets: Flow<List<VisionPresetEntity>> = visionDao.getAllPresets()
    val recentAimScores: Flow<List<AimScoreEntity>> = aimDao.getRecentScores()

    suspend fun getProfileByPackage(packageName: String): GameProfileEntity? =
        profileDao.getProfileByPackage(packageName)

    suspend fun getProfileById(id: Long): GameProfileEntity? =
        profileDao.getProfileById(id)

    suspend fun saveProfile(profile: GameProfileEntity): Long =
        profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: GameProfileEntity) =
        profileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: GameProfileEntity) =
        profileDao.deleteProfile(profile)

    fun getSessionsForGame(packageName: String): Flow<List<GameSessionEntity>> =
        sessionDao.getSessionsForGame(packageName)

    suspend fun logSession(session: GameSessionEntity): Long =
        sessionDao.insertSession(session)

    suspend fun getPresetById(id: Long): VisionPresetEntity? =
        visionDao.getPresetById(id)

    suspend fun savePreset(preset: VisionPresetEntity): Long =
        visionDao.insertPreset(preset)

    suspend fun updatePreset(preset: VisionPresetEntity) =
        visionDao.updatePreset(preset)

    suspend fun deletePreset(preset: VisionPresetEntity) =
        visionDao.deletePreset(preset)

    suspend fun getHighScore(): Int =
        aimDao.getHighScore() ?: 0

    suspend fun recordAimScore(score: AimScoreEntity): Long =
        aimDao.insertScore(score)
}
