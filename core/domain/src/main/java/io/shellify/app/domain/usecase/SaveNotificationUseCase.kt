package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.PwaNotification
import io.shellify.app.domain.repository.NotificationRepository

class SaveNotificationUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(notification: PwaNotification): Long = repo.save(notification)
}
