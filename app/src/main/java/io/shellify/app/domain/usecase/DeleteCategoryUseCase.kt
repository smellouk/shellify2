package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repo.delete(category)
}
