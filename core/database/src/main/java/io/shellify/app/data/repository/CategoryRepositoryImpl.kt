package io.shellify.app.data.repository

import io.shellify.app.data.local.dao.CategoryDao
import io.shellify.app.data.mapper.toDomain
import io.shellify.app.data.mapper.toEntity
import io.shellify.app.domain.model.Category
import io.shellify.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {
    override fun getAll(): Flow<List<Category>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? =
        dao.getById(id)?.toDomain()

    override suspend fun save(category: Category): Long {
        val entity = category.toEntity()
        return if (entity.id == 0L) dao.insert(entity)
        else {
            dao.update(entity); entity.id
        }
    }

    override suspend fun delete(category: Category) = dao.delete(category.toEntity())
    override suspend fun deleteAll() = dao.deleteAll()
}
