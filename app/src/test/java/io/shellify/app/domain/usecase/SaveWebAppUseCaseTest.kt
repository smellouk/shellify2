package io.shellify.app.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockk
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveWebAppUseCaseTest {

    private val repository = mockk<WebAppRepository>()
    private val useCase = SaveWebAppUseCase(repository)

    @Test
    fun `invoke calls repository save`() = runTest {
        val app = WebApp(name = "Test", url = "https://test.com")
        coEvery { repository.save(any()) } returns 42L

        useCase(app)

        coVerify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `invoke updates updatedAt before saving`() = runTest {
        val originalTime = 1_000_000L
        val app = WebApp(name = "Test", url = "https://test.com", updatedAt = originalTime)
        val captured = slot<WebApp>()
        coEvery { repository.save(capture(captured)) } returns 1L

        val beforeCall = System.currentTimeMillis()
        useCase(app)
        val afterCall = System.currentTimeMillis()

        val savedTime = captured.captured.updatedAt
        assertTrue("updatedAt should be >= beforeCall", savedTime >= beforeCall)
        assertTrue("updatedAt should be <= afterCall", savedTime <= afterCall)
        assertTrue("updatedAt should differ from original", savedTime != originalTime)
    }

    @Test
    fun `invoke preserves all other fields except updatedAt`() = runTest {
        val app = WebApp(
            id = 5,
            name = "MyApp",
            url = "https://myapp.com",
            isFullscreen = true,
            adBlockEnabled = false,
        )
        val captured = slot<WebApp>()
        coEvery { repository.save(capture(captured)) } returns 5L

        useCase(app)

        val saved = captured.captured
        assertEquals(5L, saved.id)
        assertEquals("MyApp", saved.name)
        assertEquals("https://myapp.com", saved.url)
        assertEquals(true, saved.isFullscreen)
        assertEquals(false, saved.adBlockEnabled)
    }

    @Test
    fun `invoke returns the id returned by the repository`() = runTest {
        val app = WebApp(name = "New App", url = "https://new.com")
        coEvery { repository.save(any()) } returns 99L

        val result = useCase(app)

        assertEquals(99L, result)
    }
}
