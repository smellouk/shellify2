package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository

class SaveCategoryUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(category: Category): Long = repo.save(category)
}
