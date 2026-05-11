package dev.pwaforge.domain.usecase

import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.repository.WebAppRepository

class DeleteWebAppUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(app: WebApp) = repo.delete(app)
}
