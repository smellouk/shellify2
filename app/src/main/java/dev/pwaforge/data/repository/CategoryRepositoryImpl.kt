package dev.pwaforge.data.repository

import dev.pwaforge.data.local.dao.CategoryDao
import dev.pwaforge.data.mapper.toDomain
import dev.pwaforge.data.mapper.toEntity
import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {
    override fun getAll(): Flow<List<Category>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun save(category: Category): Long {
        val entity = category.toEntity()
        return if (entity.id == 0L) dao.insert(entity)
        else { dao.update(entity); entity.id }
    }

    override suspend fun delete(category: Category) = dao.delete(category.toEntity())
    override suspend fun deleteAll() = dao.deleteAll()
}
