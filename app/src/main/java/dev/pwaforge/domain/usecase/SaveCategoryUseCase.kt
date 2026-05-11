package dev.pwaforge.domain.usecase

import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.repository.CategoryRepository

class SaveCategoryUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(category: Category): Long = repo.save(category)
}
