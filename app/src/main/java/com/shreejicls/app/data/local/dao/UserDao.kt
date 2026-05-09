package com.shreejicls.app.data.local.dao

import androidx.room.*
import com.shreejicls.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId AND password = :password LIMIT 1")
    suspend fun login(userId: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY name")
    fun getAllStudents(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT * FROM users WHERE role = 'STUDENT' AND (name LIKE '%' || :query || '%' OR userId LIKE '%' || :query || '%' OR studentClass LIKE '%' || :query || '%')")
    fun searchStudents(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET password = :newPassword WHERE userId = :userId")
    suspend fun changePassword(userId: String, newPassword: String)
}
