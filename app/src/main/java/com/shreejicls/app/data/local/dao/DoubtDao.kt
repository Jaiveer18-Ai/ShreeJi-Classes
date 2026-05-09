package com.shreejicls.app.data.local.dao

import androidx.room.*
import com.shreejicls.app.data.local.entity.DoubtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoubtDao {
    @Query("SELECT * FROM doubts ORDER BY createdAt DESC")
    fun getAllDoubts(): Flow<List<DoubtEntity>>

    @Query("SELECT * FROM doubts WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getDoubtsByStudent(studentId: String): Flow<List<DoubtEntity>>

    @Query("SELECT * FROM doubts WHERE status = 'OPEN' ORDER BY createdAt DESC")
    fun getOpenDoubts(): Flow<List<DoubtEntity>>

    @Query("SELECT COUNT(*) FROM doubts WHERE status = 'OPEN'")
    fun getOpenDoubtsCount(): Flow<Int>

    @Query("SELECT * FROM doubts WHERE subject = :subject ORDER BY createdAt DESC")
    fun getDoubtsBySubject(subject: String): Flow<List<DoubtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoubt(doubt: DoubtEntity)

    @Update
    suspend fun updateDoubt(doubt: DoubtEntity)

    @Query("UPDATE doubts SET reply = :reply, repliedBy = :repliedBy, replyDate = :replyDate, status = 'RESOLVED' WHERE doubtId = :doubtId")
    suspend fun replyToDoubt(doubtId: Long, reply: String, repliedBy: String, replyDate: Long)
}
