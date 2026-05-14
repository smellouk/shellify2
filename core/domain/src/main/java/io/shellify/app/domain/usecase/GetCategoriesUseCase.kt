package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val repo: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = repo.getAll()
}
