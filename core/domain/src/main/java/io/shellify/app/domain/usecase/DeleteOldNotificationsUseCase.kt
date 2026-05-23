package io.shellify.app.domain.usecase

import io.shellify.app.domain.repository.NotificationRepository

class DeleteOldNotificationsUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()) {
        repo.deleteOlderThan(now - 30L * 24 * 60 * 60 * 1000)
    }
}
