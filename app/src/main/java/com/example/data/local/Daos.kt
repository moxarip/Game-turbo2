package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AimScoreEntity
import com.example.data.model.GameProfileEntity
import com.example.data.model.GameSessionEntity
import com.example.data.model.VisionPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProfileDao {
    @Query("SELECT * FROM game_profiles ORDER BY lastPlayed DESC")
    fun getAllProfiles(): Flow<List<GameProfileEntity>>

    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getProfileByPackage(packageName: String): GameProfileEntity?

    @Query("SELECT * FROM game_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): GameProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GameProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: GameProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: GameProfileEntity)
}

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_sessions WHERE gamePackage = :packageName ORDER BY startTime DESC")
    fun getSessionsForGame(packageName: String): Flow<List<GameSessionEntity>>

    @Insert
    suspend fun insertSession(session: GameSessionEntity): Long
}

@Dao
interface VisionPresetDao {
    @Query("SELECT * FROM vision_presets ORDER BY isBuiltIn DESC, id ASC")
    fun getAllPresets(): Flow<List<VisionPresetEntity>>

    @Query("SELECT * FROM vision_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: Long): VisionPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: VisionPresetEntity): Long

    @Update
    suspend fun updatePreset(preset: VisionPresetEntity)

    @Delete
    suspend fun deletePreset(preset: VisionPresetEntity)
}

@Dao
interface AimScoreDao {
    @Query("SELECT * FROM aim_scores ORDER BY timestamp DESC LIMIT 50")
    fun getRecentScores(): Flow<List<AimScoreEntity>>

    @Query("SELECT MAX(score) FROM aim_scores")
    suspend fun getHighScore(): Int?

    @Insert
    suspend fun insertScore(score: AimScoreEntity): Long
}
