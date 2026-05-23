package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository

class GetCategoryByIdUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(id: Long): Category? = repo.getById(id)
}
