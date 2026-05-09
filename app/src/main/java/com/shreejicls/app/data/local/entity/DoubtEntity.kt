package com.shreejicls.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doubts")
data class DoubtEntity(
    @PrimaryKey(autoGenerate = true) val doubtId: Long = 0,
    val studentId: String,
    val studentName: String = "",
    val subject: String,
    val question: String,
    val questionImage: String? = null,
    val reply: String? = null,
    val repliedBy: String? = null,
    val replyDate: Long? = null,
    val status: String = "OPEN", // OPEN, RESOLVED
    val createdAt: Long = System.currentTimeMillis()
)
