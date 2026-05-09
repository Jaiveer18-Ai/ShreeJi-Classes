package com.shreejicls.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey(autoGenerate = true) val resultId: Long = 0,
    val testId: Long,
    val studentId: String,
    val marksObtained: Int,
    val totalMarks: Int = 100,
    val remarks: String = "",
    val date: Long = System.currentTimeMillis()
)
