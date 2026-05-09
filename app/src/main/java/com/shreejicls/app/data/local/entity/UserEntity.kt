package com.shreejicls.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val role: String, // "TEACHER" or "STUDENT"
    val password: String,
    val phone: String = "",
    val parentPhone: String = "",
    val studentClass: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
