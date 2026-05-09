package com.shreejicls.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fees")
data class FeeEntity(
    @PrimaryKey(autoGenerate = true) val feeId: Long = 0,
    val studentId: String,
    val month: Int,
    val year: Int,
    val amount: Double,
    val status: String = "UNPAID", // PAID, UNPAID, PARTIAL
    val paidAmount: Double = 0.0,
    val paidDate: Long? = null,
    val dueDate: Long,
    val remarks: String = ""
)
