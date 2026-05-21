package io.shellify.app.domain.usecase

import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.repository.WebAppRepository
import kotlinx.coroutines.flow.first

class FindAppsForUrlUseCase(private val repo: WebAppRepository) {
    suspend operator fun invoke(url: String): List<WebApp> {
        val incomingHost = runCatching { java.net.URI(url).host }.getOrNull()
            ?.takeIf { it.isNotBlank() } ?: return emptyList()
        return repo.getAll().first().filter { app ->
            runCatching { java.net.URI(app.url).host }.getOrNull() == incomingHost
        }
    }
}
