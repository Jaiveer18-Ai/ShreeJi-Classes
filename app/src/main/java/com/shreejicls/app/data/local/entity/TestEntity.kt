package com.shreejicls.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true) val testId: Long = 0,
    val subject: String,
    val title: String,
    val date: Long,
    val time: String = "10:00 AM",
    val syllabus: String = "",
    val totalMarks: Int = 100,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)
