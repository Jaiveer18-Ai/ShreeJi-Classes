package com.shreejicls.app.data.local.dao

import androidx.room.*
import com.shreejicls.app.data.local.entity.TestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM tests ORDER BY date DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE date >= :currentTime ORDER BY date ASC")
    fun getUpcomingTests(currentTime: Long): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE testId = :testId LIMIT 1")
    suspend fun getTestById(testId: Long): TestEntity?

    @Query("SELECT COUNT(*) FROM tests WHERE date >= :currentTime")
    fun getUpcomingTestsCount(currentTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Update
    suspend fun updateTest(test: TestEntity)

    @Delete
    suspend fun deleteTest(test: TestEntity)
}
