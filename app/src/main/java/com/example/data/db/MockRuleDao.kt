package com.example.data.db

import androidx.room.*
import com.example.data.model.MockRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MockRuleDao {
    @Query("SELECT * FROM mock_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<MockRuleEntity>>

    @Query("SELECT * FROM mock_rules WHERE isEnabled = 1")
    suspend fun getActiveRulesSync(): List<MockRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: MockRuleEntity): Long

    @Update
    suspend fun updateRule(rule: MockRuleEntity)

    @Query("UPDATE mock_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM mock_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)
}
