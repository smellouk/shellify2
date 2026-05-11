package dev.pwaforge.domain.usecase

import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val repo: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = repo.getAll()
}
