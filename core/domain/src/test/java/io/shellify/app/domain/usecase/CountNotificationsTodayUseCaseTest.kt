package io.shellify.app.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.repository.NotificationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CountNotificationsTodayUseCaseTest {

    private val repo = mockk<NotificationRepository>()
    private val useCase = CountNotificationsTodayUseCase(repo)

    @Test
    fun `invoke passes UTC day start to repository`() = runTest {
        val now = 1_700_000_000_000L
        @Suppress("MagicNumber")
        val dayStart = (now / 86_400_000L) * 86_400_000L
        coEvery { repo.countSince(any(), any()) } returns 0

        useCase(appId = 42L, now = now)

        coVerify(exactly = 1) { repo.countSince(42L, dayStart) }
    }

    @Test
    fun `invoke returns repository value`() = runTest {
        coEvery { repo.countSince(any(), any()) } returns 17

        val result = useCase(appId = 1L, now = System.currentTimeMillis())

        assertEquals(17, result)
    }
}
