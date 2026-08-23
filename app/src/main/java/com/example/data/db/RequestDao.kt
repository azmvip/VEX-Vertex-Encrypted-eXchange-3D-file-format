package com.example.data.db

import androidx.room.*
import com.example.data.model.InterceptedRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("SELECT * FROM intercepted_requests WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getRequestsForSession(sessionId: String): Flow<List<InterceptedRequestEntity>>

    @Query("SELECT * FROM intercepted_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<InterceptedRequestEntity>>

    @Query("SELECT * FROM intercepted_requests WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedRequests(): Flow<List<InterceptedRequestEntity>>

    @Query("SELECT * FROM intercepted_requests WHERE id = :id")
    suspend fun getRequestById(id: Long): InterceptedRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: InterceptedRequestEntity): Long

    @Update
    suspend fun updateRequest(request: InterceptedRequestEntity)

    @Query("UPDATE intercepted_requests SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmarked(id: Long, isBookmarked: Boolean)

    @Query("DELETE FROM intercepted_requests WHERE sessionId = :sessionId")
    suspend fun clearSession(sessionId: String)

    @Query("DELETE FROM intercepted_requests")
    suspend fun clearAll()

    @Query("DELETE FROM intercepted_requests WHERE id = :id")
    suspend fun deleteById(id: Long)
}
