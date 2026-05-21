package io.shellify.app.presentation.linkdispatcher

import io.shellify.app.domain.model.WebApp

data class LinkDispatcherUiState(
    val sheet: DispatchSheet = DispatchSheet.None,
)

sealed interface DispatchSheet {
    data object None : DispatchSheet
    data class Chooser(val matches: List<WebApp>, val url: String) : DispatchSheet
}

sealed interface LinkDispatcherCommand {
    data class LaunchApp(val app: WebApp, val url: String) : LinkDispatcherCommand
    data class AddAsNew(val url: String) : LinkDispatcherCommand
    data object FallbackToBrowser : LinkDispatcherCommand
}
