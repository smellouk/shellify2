package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository

class SaveWebAppUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(app: WebApp): Long =
        repo.save(app.copy(updatedAt = System.currentTimeMillis()))
}
