package com.shreejicls.app.data.local.dao

import androidx.room.*
import com.shreejicls.app.data.local.entity.FeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {
    @Query("SELECT * FROM fees WHERE studentId = :studentId ORDER BY year DESC, month DESC")
    fun getFeesByStudent(studentId: String): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees ORDER BY year DESC, month DESC")
    fun getAllFees(): Flow<List<FeeEntity>>

    @Query("SELECT COUNT(*) FROM fees WHERE status = 'UNPAID' OR status = 'PARTIAL'")
    fun getPendingFeesCount(): Flow<Int>

    @Query("SELECT * FROM fees WHERE status = 'UNPAID' OR status = 'PARTIAL'")
    fun getPendingFees(): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fees WHERE studentId = :studentId AND month = :month AND year = :year LIMIT 1")
    suspend fun getFeeForMonth(studentId: String, month: Int, year: Int): FeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeEntity)

    @Update
    suspend fun updateFee(fee: FeeEntity)

    @Delete
    suspend fun deleteFee(fee: FeeEntity)

    @Query("UPDATE fees SET status = :status, paidAmount = :paidAmount, paidDate = :paidDate WHERE feeId = :feeId")
    suspend fun updateFeeStatus(feeId: Long, status: String, paidAmount: Double, paidDate: Long?)
}
