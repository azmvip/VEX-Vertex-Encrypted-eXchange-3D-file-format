package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_sessions")
data class SavedSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val requestCount: Int = 0,
    val totalBytes: Long = 0,
    val domainsSummary: String = ""
)

data class ConsoleLogItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "log", // log, info, warn, error
    val message: String,
    val source: String? = null
)
