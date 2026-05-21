package io.shellify.app.presentation.linkdispatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.usecase.FindAppsForUrlUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LinkDispatcherViewModel(
    private val findAppsForUrl: FindAppsForUrlUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkDispatcherUiState())
    val uiState: StateFlow<LinkDispatcherUiState> = _uiState.asStateFlow()

    private val _commands = MutableSharedFlow<LinkDispatcherCommand>(replay = 1, extraBufferCapacity = 16)
    val commands: SharedFlow<LinkDispatcherCommand> = _commands.asSharedFlow()

    fun dispatch(url: String) {
        viewModelScope.launch {
            val matches = findAppsForUrl(url)
            when (matches.size) {
                0 -> _commands.emit(LinkDispatcherCommand.AddAsNew(url))
                1 -> _commands.emit(LinkDispatcherCommand.LaunchApp(matches[0], url))
                else -> _uiState.update { it.copy(sheet = DispatchSheet.Chooser(matches, url)) }
            }
        }
    }

    fun onAppSelected(app: WebApp, url: String) {
        viewModelScope.launch { _commands.emit(LinkDispatcherCommand.LaunchApp(app, url)) }
    }

    fun onAddAsNew(url: String) {
        viewModelScope.launch { _commands.emit(LinkDispatcherCommand.AddAsNew(url)) }
    }

    fun onDismiss() {
        viewModelScope.launch { _commands.emit(LinkDispatcherCommand.FallbackToBrowser) }
    }

    class Factory(
        private val findAppsForUrl: FindAppsForUrlUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LinkDispatcherViewModel(findAppsForUrl) as T
    }
}
