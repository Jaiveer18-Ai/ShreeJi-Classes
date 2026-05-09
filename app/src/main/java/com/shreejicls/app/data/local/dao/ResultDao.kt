package com.shreejicls.app.data.local.dao

import androidx.room.*
import com.shreejicls.app.data.local.entity.ResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Query("SELECT * FROM results WHERE studentId = :studentId ORDER BY date DESC")
    fun getResultsByStudent(studentId: String): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results WHERE testId = :testId ORDER BY marksObtained DESC")
    fun getResultsByTest(testId: Long): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results ORDER BY date DESC")
    fun getAllResults(): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results WHERE studentId = :studentId ORDER BY date DESC LIMIT 5")
    fun getRecentResults(studentId: String): Flow<List<ResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultEntity)

    @Update
    suspend fun updateResult(result: ResultEntity)

    @Delete
    suspend fun deleteResult(result: ResultEntity)
}
