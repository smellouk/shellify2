package dev.pwaforge.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.core.backup.BackupSchedule
import dev.pwaforge.core.backup.BackupSettings
import dev.pwaforge.core.security.PasswordManager
import dev.pwaforge.core.theme.ThemeManager
import dev.pwaforge.core.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val page: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: Int? = null,
    val passwordSet: Boolean = false,
    val backupEnabled: Boolean = false,
    val backupDirectoryUri: String? = null,
    val backupSchedule: BackupSchedule = BackupSchedule.NONE,
)

class OnboardingViewModel(
    private val themeManager: ThemeManager,
    private val passwordManager: PasswordManager,
    private val backupSettings: BackupSettings,
    private val onFinished: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _state

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    page             = themeManager.onboardingPage.first(),
                    passwordSet      = passwordManager.passwordHash.first() != null,
                    backupEnabled    = backupSettings.enabled.first(),
                    backupDirectoryUri = backupSettings.directoryUri.first(),
                    backupSchedule   = backupSettings.schedule.first(),
                    accentColor      = themeManager.accentColor.first(),
                    themeMode        = themeManager.themeMode.first(),
                )
            }
        }
    }

    fun goTo(page: Int) {
        _state.update { it.copy(page = page) }
        viewModelScope.launch { themeManager.saveOnboardingPage(page) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _state.update { it.copy(themeMode = mode) }
        viewModelScope.launch { themeManager.setThemeMode(mode) }
    }

    fun setAccentColor(color: Int?) {
        _state.update { it.copy(accentColor = color) }
        viewModelScope.launch { themeManager.setAccentColor(color) }
    }

    fun setPassword(password: String) {
        viewModelScope.launch {
            passwordManager.setPassword(password)
            _state.update { it.copy(passwordSet = true) }
        }
    }

    fun setBackupEnabled(v: Boolean) {
        _state.update { it.copy(backupEnabled = v) }
        viewModelScope.launch { backupSettings.setEnabled(v) }
    }

    fun setBackupDirectoryUri(uri: String) {
        _state.update { it.copy(backupDirectoryUri = uri) }
        viewModelScope.launch { backupSettings.setDirectoryUri(uri) }
    }

    fun setBackupSchedule(schedule: BackupSchedule) {
        _state.update { it.copy(backupSchedule = schedule) }
        viewModelScope.launch { backupSettings.setSchedule(schedule) }
    }

    fun finish() {
        viewModelScope.launch {
            themeManager.setOnboardingDone()
            onFinished()
        }
    }
}
