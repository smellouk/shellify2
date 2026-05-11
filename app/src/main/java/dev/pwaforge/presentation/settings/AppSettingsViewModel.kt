package dev.pwaforge.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.core.isolation.IsolationManager
import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.repository.WebAppRepository
import dev.pwaforge.domain.usecase.DeleteWebAppUseCase
import dev.pwaforge.domain.usecase.SaveWebAppUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppSettingsUiState(
    val app: WebApp? = null,
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
)

class AppSettingsViewModel(
    private val appId: Long,
    private val repo: WebAppRepository,
    private val saveWebApp: SaveWebAppUseCase,
    private val deleteWebApp: DeleteWebAppUseCase,
    private val isolationManager: IsolationManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _state

    init {
        viewModelScope.launch {
            val app = repo.getById(appId)
            _state.update { it.copy(app = app, isLoading = false) }
        }
    }

    fun update(transform: (WebApp) -> WebApp) {
        val app = _state.value.app ?: return
        val updated = transform(app)
        _state.update { it.copy(app = updated) }
        viewModelScope.launch { saveWebApp(updated) }
    }

    fun toggleFullscreen() = update { it.copy(isFullscreen = !it.isFullscreen) }
    fun toggleAdBlock() = update { it.copy(adBlockEnabled = !it.adBlockEnabled) }
    fun toggleTranslate() = update { it.copy(translateEnabled = !it.translateEnabled) }

    fun showDeleteDialog() = _state.update { it.copy(showDeleteDialog = true) }
    fun dismissDeleteDialog() = _state.update { it.copy(showDeleteDialog = false) }

    fun deleteApp() {
        val app = _state.value.app ?: return
        viewModelScope.launch {
            isolationManager.clearData(app.isolationId)
            deleteWebApp(app)
            _state.update { it.copy(deleted = true) }
        }
    }
}
