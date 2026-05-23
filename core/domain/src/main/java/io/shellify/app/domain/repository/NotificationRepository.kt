package io.shellify.app.domain.repository

import io.shellify.app.domain.model.PwaNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getByApp(appId: Long): Flow<List<PwaNotification>>
    suspend fun save(notification: PwaNotification): Long
    suspend fun deleteOlderThan(cutoff: Long)
    suspend fun deleteByApp(appId: Long)
    suspend fun countSince(appId: Long, sinceMillis: Long): Int
}
