package io.shellify.app.domain.repository

import io.shellify.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    suspend fun save(category: Category): Long
    suspend fun delete(category: Category)
    suspend fun deleteAll()
}
