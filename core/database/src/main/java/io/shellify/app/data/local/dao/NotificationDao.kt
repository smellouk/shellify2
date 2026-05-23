package io.shellify.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.shellify.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE app_id = :appId ORDER BY timestamp DESC")
    fun getByApp(appId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("DELETE FROM notifications WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM notifications WHERE app_id = :appId")
    suspend fun deleteByApp(appId: Long)

    @Query("SELECT COUNT(*) FROM notifications WHERE app_id = :appId AND timestamp >= :sinceMillis")
    suspend fun countSince(appId: Long, sinceMillis: Long): Int
}
