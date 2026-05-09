package com.shreejicls.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val noteId: Long = 0,
    val title: String,
    val subject: String,
    val topic: String = "",
    val type: String = "LINK", // PDF, IMAGE, LINK, VIDEO
    val content: String, // URL or file path
    val description: String = "",
    val uploadedBy: String,
    val uploadDate: Long = System.currentTimeMillis()
)
