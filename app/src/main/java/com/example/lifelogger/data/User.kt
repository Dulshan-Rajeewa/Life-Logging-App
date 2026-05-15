package com.example.lifelogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "users")
data class User(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @SerialName("full_name")
    val fullName: String,
    
    val email: String,
    val password: String,
    
    @kotlinx.serialization.Transient
    val isLoggedIn: Boolean = false
)