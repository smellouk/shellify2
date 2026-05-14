package io.shellify.app.domain.usecase

import io.shellify.app.domain.repository.CategoryRepository

class DeleteAllCategoriesUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke() = repo.deleteAll()
}
