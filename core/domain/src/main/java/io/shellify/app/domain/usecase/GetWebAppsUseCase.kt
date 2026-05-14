package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.flow.Flow

class GetWebAppsUseCase(private val repo: WebAppRepository) {
    operator fun invoke(): Flow<List<WebApp>> = repo.getAll()
}
