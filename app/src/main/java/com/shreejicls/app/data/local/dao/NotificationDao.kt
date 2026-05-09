package com.shreejicls.app.data.local.dao

import androidx.room.*
import com.shreejicls.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE targetUserId = :userId OR targetUserId = 'ALL' ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (targetUserId = :userId OR targetUserId = 'ALL') AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE notifId = :notifId")
    suspend fun markAsRead(notifId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetUserId = :userId OR targetUserId = 'ALL'")
    suspend fun markAllAsRead(userId: String)
}
