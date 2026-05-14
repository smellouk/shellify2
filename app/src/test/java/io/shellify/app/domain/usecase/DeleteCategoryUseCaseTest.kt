package io.shellify.app.domain.usecase

import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteCategoryUseCaseTest {

    private val repository = mockk<CategoryRepository>()
    private val useCase = DeleteCategoryUseCase(repository)

    @Test
    fun `invoke delegates delete to repository`() = runTest {
        val category = Category(id = 1, name = "Work", icon = "work", color = "#FF0000")
        coJustRun { repository.delete(category) }

        useCase(category)

        coVerify(exactly = 1) { repository.delete(category) }
    }

    @Test
    fun `invoke passes exact same category object to repository`() = runTest {
        val category = Category(id = 42, name = "Personal", icon = "person", color = "#00FF00")
        coJustRun { repository.delete(any()) }

        useCase(category)

        coVerify { repository.delete(category) }
    }
}
