package com.shreejicls.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val notifId: Long = 0,
    val title: String,
    val body: String,
    val type: String, // FEE_REMINDER, NEW_NOTE, TEST_SCHEDULED, DOUBT_REPLY, MARKS_UPLOADED
    val targetUserId: String = "ALL",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
