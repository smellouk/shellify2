package io.shellify.app.data.mapper

import io.shellify.app.data.local.entity.NotificationEntity
import io.shellify.app.domain.model.PwaNotification

fun NotificationEntity.toDomain(): PwaNotification = PwaNotification(
    id = id,
    appId = appId,
    title = title,
    body = body,
    iconUrl = iconUrl,
    timestamp = timestamp,
    isRead = isRead,
)

fun PwaNotification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    appId = appId,
    title = title,
    body = body,
    iconUrl = iconUrl,
    timestamp = timestamp,
    isRead = isRead,
)
