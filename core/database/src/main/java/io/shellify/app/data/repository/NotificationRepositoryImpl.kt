package io.shellify.app.data.repository

import io.shellify.app.data.local.dao.NotificationDao
import io.shellify.app.data.mapper.toDomain
import io.shellify.app.data.mapper.toEntity
import io.shellify.app.domain.model.PwaNotification
import io.shellify.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(private val dao: NotificationDao) : NotificationRepository {

    override fun getByApp(appId: Long): Flow<List<PwaNotification>> =
        dao.getByApp(appId).map { list -> list.map { it.toDomain() } }

    override suspend fun save(notification: PwaNotification): Long =
        dao.insert(notification.toEntity())

    override suspend fun deleteOlderThan(cutoff: Long) = dao.deleteOlderThan(cutoff)

    override suspend fun deleteByApp(appId: Long) = dao.deleteByApp(appId)

    override suspend fun countSince(appId: Long, sinceMillis: Long): Int = dao.countSince(appId, sinceMillis)
}
