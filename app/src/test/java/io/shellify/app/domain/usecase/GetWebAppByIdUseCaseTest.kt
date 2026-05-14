package io.shellify.app.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetWebAppByIdUseCaseTest {

    private val repository = mockk<WebAppRepository>()
    private val useCase = GetWebAppByIdUseCase(repository)

    @Test
    fun `invoke returns web app when found`() = runTest {
        val expected = WebApp(id = 5, name = "GitHub", url = "https://github.com")
        coEvery { repository.getById(5L) } returns expected

        val result = useCase(5L)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns null when not found`() = runTest {
        coEvery { repository.getById(99L) } returns null

        val result = useCase(99L)

        assertNull(result)
    }

    @Test
    fun `invoke passes correct id to repository`() = runTest {
        coEvery { repository.getById(any()) } returns null

        useCase(42L)

        coVerify(exactly = 1) { repository.getById(42L) }
    }
}
