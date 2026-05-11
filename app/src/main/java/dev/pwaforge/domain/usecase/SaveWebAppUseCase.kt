package dev.pwaforge.domain.usecase

import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.repository.WebAppRepository

class SaveWebAppUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(app: WebApp): Long = repo.save(app.copy(updatedAt = System.currentTimeMillis()))
}
