package com.example.lifelogger.data

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseLog(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val image_file_path: String? = null,
    val audio_file_path: String? = null,
    val category: String = "General",
    val user_id: String,
    val is_synced_with_cloud: Boolean = true
)