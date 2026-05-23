package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.PwaNotification
import io.shellify.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(private val repo: NotificationRepository) {
    operator fun invoke(appId: Long): Flow<List<PwaNotification>> = repo.getByApp(appId)
}
