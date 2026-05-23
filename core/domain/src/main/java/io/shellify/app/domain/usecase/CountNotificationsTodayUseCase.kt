package io.shellify.app.domain.usecase

import io.shellify.app.domain.repository.NotificationRepository

class CountNotificationsTodayUseCase(private val repo: NotificationRepository) {

    suspend operator fun invoke(appId: Long, now: Long = System.currentTimeMillis()): Int {
        val dayStart = (now / DAY_MILLIS) * DAY_MILLIS
        return repo.countSince(appId, dayStart)
    }

    private companion object {
        @Suppress("MagicNumber")
        const val DAY_MILLIS = 86_400_000L
    }
}
