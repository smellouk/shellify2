package io.shellify.app.domain.repository

import io.shellify.app.domain.model.WebApp
import kotlinx.coroutines.flow.Flow

interface WebAppRepository {
    fun getAll(): Flow<List<WebApp>>
    fun getByCategory(categoryId: Long): Flow<List<WebApp>>
    suspend fun getById(id: Long): WebApp?
    suspend fun getByUrl(url: String): WebApp?
    suspend fun getByName(name: String): WebApp?
    suspend fun save(app: WebApp): Long
    suspend fun delete(app: WebApp)
    suspend fun deleteById(id: Long)
    suspend fun deleteAll()
    suspend fun clearIsolatedData(isolationId: String)
}
