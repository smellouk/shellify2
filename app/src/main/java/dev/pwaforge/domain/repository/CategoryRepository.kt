package dev.pwaforge.domain.repository

import dev.pwaforge.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    suspend fun save(category: Category): Long
    suspend fun delete(category: Category)
    suspend fun deleteAll()
}
