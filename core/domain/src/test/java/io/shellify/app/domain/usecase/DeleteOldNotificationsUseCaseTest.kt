package io.shellify.app.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.repository.NotificationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteOldNotificationsUseCaseTest {

    private val repository = mockk<NotificationRepository>(relaxed = true)
    private val useCase = DeleteOldNotificationsUseCase(repository)

    @Test
    fun `invoke calls deleteOlderThan with cutoff 30 days before now`() = runTest {
        val now = 1_000_000_000_000L
        val expectedCutoff = now - 30L * 86_400_000L

        useCase(now = now)

        coVerify(exactly = 1) { repository.deleteOlderThan(expectedCutoff) }
    }
}
