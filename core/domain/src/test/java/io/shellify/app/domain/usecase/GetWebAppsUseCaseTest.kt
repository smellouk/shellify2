package io.shellify.app.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWebAppsUseCaseTest {

    private val repository = mockk<WebAppRepository>()
    private val useCase = GetWebAppsUseCase(repository)

    @Test
    fun `invoke delegates to repository getAll`() {
        every { repository.getAll() } returns flowOf(emptyList())
        useCase()
        verify(exactly = 1) { repository.getAll() }
    }

    @Test
    fun `invoke returns flow emitting repository data`() = runTest {
        val apps = listOf(WebApp(name = "Test App", url = "https://test.com"))
        every { repository.getAll() } returns flowOf(apps)

        val emitted = useCase().toList()
        assertEquals(1, emitted.size)
        assertEquals(apps, emitted[0])
    }

    @Test
    fun `invoke returns empty flow when repository has no apps`() = runTest {
        every { repository.getAll() } returns flowOf(emptyList())

        val emitted = useCase().toList()
        assertEquals(listOf(emptyList<WebApp>()), emitted)
    }
}
