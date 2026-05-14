package io.shellify.app.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveCategoryUseCaseTest {

    private val repository = mockk<CategoryRepository>()
    private val useCase = SaveCategoryUseCase(repository)

    @Test
    fun `invoke delegates to repository save`() = runTest {
        val category = Category(name = "Work")
        coEvery { repository.save(category) } returns 1L

        useCase(category)

        coVerify(exactly = 1) { repository.save(category) }
    }

    @Test
    fun `invoke returns id from repository`() = runTest {
        val category = Category(name = "Gaming")
        coEvery { repository.save(category) } returns 77L

        val result = useCase(category)

        assertEquals(77L, result)
    }

    @Test
    fun `invoke passes exact category to repository`() = runTest {
        val category =
            Category(id = 3, name = "Finance", sortIndex = 2, icon = "wallet", color = "#16a34a")
        coEvery { repository.save(category) } returns 3L

        useCase(category)

        coVerify { repository.save(category) }
    }
}
