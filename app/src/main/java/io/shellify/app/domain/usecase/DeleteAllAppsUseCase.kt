package io.shellify.app.domain.usecase

import io.shellify.app.domain.repository.WebAppRepository

class DeleteAllAppsUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke() = repo.deleteAll()
}
