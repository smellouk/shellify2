package io.shellify.app.domain.usecase

import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.repository.CategoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteAllCategoriesUseCaseTest {

    private val repository = mockk<CategoryRepository>()
    private val useCase = DeleteAllCategoriesUseCase(repository)

    @Test
    fun `invoke delegates deleteAll to repository`() = runTest {
        coJustRun { repository.deleteAll() }

        useCase()

        coVerify(exactly = 1) { repository.deleteAll() }
    }
}
