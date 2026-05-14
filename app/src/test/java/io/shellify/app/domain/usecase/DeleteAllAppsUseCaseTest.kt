package io.shellify.app.domain.usecase

import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteAllAppsUseCaseTest {

    private val repository = mockk<WebAppRepository>()
    private val useCase = DeleteAllAppsUseCase(repository)

    @Test
    fun `invoke delegates deleteAll to repository`() = runTest {
        coJustRun { repository.deleteAll() }

        useCase()

        coVerify(exactly = 1) { repository.deleteAll() }
    }
}
