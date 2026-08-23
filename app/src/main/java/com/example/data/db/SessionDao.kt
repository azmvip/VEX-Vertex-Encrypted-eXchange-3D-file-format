package com.example.data.db

import androidx.room.*
import com.example.data.model.SavedSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM saved_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SavedSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SavedSessionEntity)

    @Query("DELETE FROM saved_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}
