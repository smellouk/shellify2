package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository

class GetWebAppByNameUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(name: String): WebApp? = repo.getByName(name)
}
