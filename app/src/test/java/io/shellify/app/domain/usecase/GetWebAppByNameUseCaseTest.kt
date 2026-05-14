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

class GetWebAppByNameUseCaseTest {

    private val repository = mockk<WebAppRepository>()
    private val useCase = GetWebAppByNameUseCase(repository)

    @Test
    fun `invoke returns web app when name matches`() = runTest {
        val expected = WebApp(id = 3, name = "Gmail", url = "https://mail.google.com")
        coEvery { repository.getByName("Gmail") } returns expected

        val result = useCase("Gmail")

        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns null when name not found`() = runTest {
        coEvery { repository.getByName(any()) } returns null

        val result = useCase("NonExistent")

        assertNull(result)
    }

    @Test
    fun `invoke passes exact name to repository`() = runTest {
        coEvery { repository.getByName(any()) } returns null

        useCase("Twitter")

        coVerify(exactly = 1) { repository.getByName("Twitter") }
    }
}
