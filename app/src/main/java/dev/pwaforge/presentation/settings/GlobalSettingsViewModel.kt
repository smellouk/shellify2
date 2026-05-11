package dev.pwaforge.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.core.isolation.IsolationManager
import dev.pwaforge.core.theme.ThemeManager
import dev.pwaforge.core.theme.ThemeMode
import dev.pwaforge.domain.model.UserAgentMode
import dev.pwaforge.domain.repository.WebAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GlobalSettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultUaMode: UserAgentMode = UserAgentMode.CHROME_MOBILE,
    val showClearAllDialog: Boolean = false,
)

class GlobalSettingsViewModel(
    private val themeManager: ThemeManager,
    private val isolationManager: IsolationManager,
    private val repo: WebAppRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GlobalSettingsUiState())
    val uiState: StateFlow<GlobalSettingsUiState> = _state

    init {
        viewModelScope.launch {
            themeManager.themeMode.collect { mode -> _state.update { it.copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            themeManager.dynamicColor.collect { enabled -> _state.update { it.copy(dynamicColor = enabled) } }
        }
        viewModelScope.launch {
            themeManager.defaultUaMode.collect { mode -> _state.update { it.copy(defaultUaMode = mode) } }
        }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { themeManager.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { themeManager.setDynamicColor(enabled) }
    fun setDefaultUaMode(mode: UserAgentMode) = viewModelScope.launch { themeManager.setDefaultUaMode(mode) }

    fun showClearAllDialog() = _state.update { it.copy(showClearAllDialog = true) }
    fun dismissClearAllDialog() = _state.update { it.copy(showClearAllDialog = false) }

    fun clearAll() = viewModelScope.launch {
        repo.getAll().first().forEach { app -> isolationManager.clearData(app.isolationId) }
        _state.update { it.copy(showClearAllDialog = false) }
    }
}
