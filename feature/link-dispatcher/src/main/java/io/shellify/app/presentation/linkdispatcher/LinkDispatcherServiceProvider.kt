package io.shellify.app.presentation.linkdispatcher

import io.shellify.app.core.navigation.WebViewIntentFactory
import io.shellify.app.domain.usecase.FindAppsForUrlUseCase

interface LinkDispatcherServiceProvider {
    val findAppsForUrl: FindAppsForUrlUseCase
    val webViewIntentFactory: WebViewIntentFactory
}
