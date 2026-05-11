package dev.pwaforge.domain.usecase

import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.repository.WebAppRepository
import kotlinx.coroutines.flow.Flow

class GetWebAppsUseCase(private val repo: WebAppRepository) {
    operator fun invoke(): Flow<List<WebApp>> = repo.getAll()
}
