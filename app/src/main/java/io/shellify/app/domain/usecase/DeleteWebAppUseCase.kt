package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository

class DeleteWebAppUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(app: WebApp) = repo.delete(app)
}
