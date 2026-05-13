package dev.pwaforge.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.core.backup.BackupSchedule
import dev.pwaforge.core.backup.BackupSettings
import dev.pwaforge.core.pwa.FaviconFetcher
import dev.pwaforge.core.pwa.PwaAnalyzer
import dev.pwaforge.core.security.PasswordManager
import dev.pwaforge.core.theme.ThemeManager
import dev.pwaforge.core.theme.ThemeMode
import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.usecase.SaveWebAppUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.UUID

sealed interface QuickPicksStatus {
    data object Idle : QuickPicksStatus
    data class Adding(val done: Int, val total: Int) : QuickPicksStatus
    data object Done : QuickPicksStatus
}

data class OnboardingUiState(
    val page: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: Int? = null,
    val passwordSet: Boolean = false,
    val backupEnabled: Boolean = false,
    val backupDirectoryUri: String? = null,
    val backupSchedule: BackupSchedule = BackupSchedule.NONE,
    val pickedAppIds: List<String> = emptyList(),
    val quickPicksStatus: QuickPicksStatus = QuickPicksStatus.Idle,
)

class OnboardingViewModel(
    private val themeManager: ThemeManager,
    private val passwordManager: PasswordManager,
    private val backupSettings: BackupSettings,
    private val saveWebApp: SaveWebAppUseCase,
    private val pwaAnalyzer: PwaAnalyzer,
    private val faviconFetcher: FaviconFetcher,
    private val onFinished: () -> Unit,
) : ViewModel() {

    private var addJob: Job? = null

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

    fun togglePickedApp(id: String) {
        _state.update { s ->
            s.copy(
                pickedAppIds = if (id in s.pickedAppIds) s.pickedAppIds - id
                               else s.pickedAppIds + id,
            )
        }
    }

    fun addPickedApps(apps: List<Pair<String, String>>) {
        addJob?.cancel()
        _state.update { it.copy(quickPicksStatus = QuickPicksStatus.Adding(0, apps.size)) }
        addJob = viewModelScope.launch {
            supervisorScope {
                val jobs = apps.map { (name, rawUrl) ->
                    async {
                        val fullUrl = if (rawUrl.startsWith("http")) rawUrl else "https://$rawUrl"
                        try {
                            val manifest = pwaAnalyzer.analyze(fullUrl)
                            val isolationId = UUID.randomUUID().toString()
                            val fetchedIconPath = faviconFetcher.fetch(
                                manifest.bestIconUrl(fullUrl), fullUrl, isolationId,
                            )
                            saveWebApp(
                                WebApp(
                                    name = manifest.name?.takeIf { it.isNotBlank() }
                                        ?: manifest.shortName?.takeIf { it.isNotBlank() }
                                        ?: name,
                                    url = fullUrl,
                                    iconSource = dev.pwaforge.domain.model.IconSource.fromLegacyPath(fetchedIconPath),
                                    themeColor = manifest.themeColor,
                                    backgroundColor = manifest.backgroundColor,
                                    description = manifest.description,
                                    isolationId = isolationId,
                                    adBlockEnabled = true,
                                ),
                            )
                        } catch (_: Exception) {
                            saveWebApp(WebApp(name = name, url = fullUrl))
                        }
                        _state.update { s ->
                            val cur = s.quickPicksStatus
                            if (cur is QuickPicksStatus.Adding)
                                s.copy(quickPicksStatus = cur.copy(done = cur.done + 1))
                            else s
                        }
                    }
                }
                jobs.awaitAll()
            }
            _state.update { it.copy(quickPicksStatus = QuickPicksStatus.Done) }
        }
    }

    fun cancelQuickPicks() {
        addJob?.cancel()
        addJob = null
        _state.update { it.copy(quickPicksStatus = QuickPicksStatus.Idle) }
    }

    fun finish() {
        viewModelScope.launch {
            themeManager.setOnboardingDone()
            onFinished()
        }
    }
}
