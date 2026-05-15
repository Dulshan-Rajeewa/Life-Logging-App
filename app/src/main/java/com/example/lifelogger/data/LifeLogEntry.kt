package com.example.lifelogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// @Entity tells Room that this class represents a table in the offline SQLite database
@Entity(tableName = "life_logs")
data class LifeLogEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Unique ID for every log
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),

    // File paths for media attachments
    val imageFilePath: String? = null,
    val audioFilePath: String? = null,

    val category: String = "General", // New category field

    // A flag to check if we need to upload this to Supabase when the internet connects
    val isSyncedWithCloud: Boolean = false,
    
    // The user who created this log (null if guest/offline)
    val userId: String? = null
)