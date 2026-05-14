package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository

class GetWebAppByIdUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(id: Long): WebApp? = repo.getById(id)
}
