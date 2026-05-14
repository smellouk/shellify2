package io.shellify.app.domain.usecase

import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteWebAppUseCaseTest {

    private val repository = mockk<WebAppRepository>()
    private val useCase = DeleteWebAppUseCase(repository)

    @Test
    fun `invoke delegates delete to repository`() = runTest {
        val app = WebApp(id = 10, name = "ToDelete", url = "https://delete.me")
        coJustRun { repository.delete(app) }

        useCase(app)

        coVerify(exactly = 1) { repository.delete(app) }
    }

    @Test
    fun `invoke passes exact same app object to repository`() = runTest {
        val app = WebApp(
            id = 7,
            name = "Specific App",
            url = "https://specific.com",
            isFullscreen = true,
        )
        coJustRun { repository.delete(any()) }

        useCase(app)

        coVerify { repository.delete(app) }
    }
}
