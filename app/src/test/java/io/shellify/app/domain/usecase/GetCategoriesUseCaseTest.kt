package io.shellify.app.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCategoriesUseCaseTest {

    private val repository = mockk<CategoryRepository>()
    private val useCase = GetCategoriesUseCase(repository)

    @Test
    fun `invoke delegates to repository getAll`() {
        every { repository.getAll() } returns flowOf(emptyList())
        useCase()
        verify(exactly = 1) { repository.getAll() }
    }

    @Test
    fun `invoke returns flow with categories from repository`() = runTest {
        val categories = listOf(
            Category(id = 1, name = "Work"),
            Category(id = 2, name = "Personal"),
        )
        every { repository.getAll() } returns flowOf(categories)

        val emitted = useCase().toList()
        assertEquals(1, emitted.size)
        assertEquals(categories, emitted[0])
    }

    @Test
    fun `invoke returns empty flow when repository has no categories`() = runTest {
        every { repository.getAll() } returns flowOf(emptyList())

        val emitted = useCase().toList()
        assertEquals(listOf(emptyList<Category>()), emitted)
    }
}
