package com.example.project
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User")
data class User(
    val userId: String,
    val username: String,
    val password: String,
    val displayName: String?,
    val accountType: Int, // 0 for Agent, 1 for Driver
    val email: String?,
    val businessPhone: String?
)
