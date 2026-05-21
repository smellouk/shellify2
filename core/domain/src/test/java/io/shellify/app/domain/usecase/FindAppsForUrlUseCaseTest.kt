package io.shellify.app.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FindAppsForUrlUseCaseTest {

    private val repository = mockk<WebAppRepository>()
    private val useCase = FindAppsForUrlUseCase(repository)

    @Test
    fun `invoke returns matching app when incoming host matches exactly one app`() = runTest {
        val apps = listOf(
            WebApp(id = 1, name = "GitHub", url = "https://github.com/explore"),
            WebApp(id = 2, name = "GitLab", url = "https://gitlab.com"),
        )
        every { repository.getAll() } returns flowOf(apps)

        val result = useCase("https://github.com/issues")

        assertEquals(1, result.size)
        assertEquals(apps[0], result[0])
    }

    @Test
    fun `invoke returns empty list when no host matches`() = runTest {
        every { repository.getAll() } returns flowOf(
            listOf(WebApp(id = 1, name = "GitHub", url = "https://github.com"))
        )

        val result = useCase("https://example.com")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns empty list for malformed url without crashing`() = runTest {
        every { repository.getAll() } returns flowOf(emptyList())

        val result = useCase("not-a-url")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns multiple matches when several apps share the same host`() = runTest {
        val apps = listOf(
            WebApp(id = 1, name = "GitHub Main", url = "https://github.com"),
            WebApp(id = 2, name = "GitHub Org", url = "https://github.com/org/repo"),
            WebApp(id = 3, name = "GitLab", url = "https://gitlab.com"),
        )
        every { repository.getAll() } returns flowOf(apps)

        val result = useCase("https://github.com")

        assertEquals(2, result.size)
        assertTrue(result.contains(apps[0]))
        assertTrue(result.contains(apps[1]))
    }

    @Test
    fun `invoke does not match subdomain as exact host match`() = runTest {
        val apps = listOf(
            WebApp(id = 1, name = "GitHub", url = "https://github.com"),
        )
        every { repository.getAll() } returns flowOf(apps)

        val result = useCase("https://api.github.com/v3")

        assertTrue(result.isEmpty())
    }
}
