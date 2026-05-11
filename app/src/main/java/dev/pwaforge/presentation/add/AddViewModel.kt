package dev.pwaforge.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.core.pwa.FaviconFetcher
import dev.pwaforge.core.pwa.PwaAnalyzer
import dev.pwaforge.domain.model.PwaManifest
import dev.pwaforge.domain.model.TranslateEngine
import dev.pwaforge.domain.model.TranslateLanguage
import dev.pwaforge.domain.model.UserAgentMode
import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.repository.WebAppRepository
import dev.pwaforge.domain.usecase.GetCategoriesUseCase
import dev.pwaforge.domain.usecase.SaveWebAppUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddUiState(
    val isLoading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val url: String = "",
    val iconPath: String? = null,
    val themeColor: String? = null,
    val categoryId: Long? = null,
    // Isolation ID — stable for the lifetime of this edit session
    val isolationId: String = UUID.randomUUID().toString(),
    // Analysis report waiting for user approval
    val pendingManifest: PwaManifest? = null,
    val pendingIconPath: String? = null,    // icon fetched during analysis, not yet applied
    // Fullscreen
    val isFullscreen: Boolean = false,
    val fullscreenShowStatusBar: Boolean = false,
    val fullscreenShowNavBar: Boolean = false,
    val fullscreenShowTopToolbar: Boolean = false,
    // Ad blocking
    val adBlockEnabled: Boolean = true,
    val adBlockAllowUserToggle: Boolean = false,
    val adBlockCustomRules: List<String> = emptyList(),
    val adBlockCustomRuleInput: String = "",
    // Translation
    val translateEnabled: Boolean = false,
    val translateTarget: TranslateLanguage = TranslateLanguage.ENGLISH,
    val translateEngine: TranslateEngine = TranslateEngine.AUTO,
    val showTranslateButton: Boolean = true,
    val autoTranslateOnLoad: Boolean = false,
    // Browser
    val uaMode: UserAgentMode = UserAgentMode.CHROME_MOBILE,
    val analyzeError: String? = null,
    val urlError: String? = null,
    val saved: Boolean = false,
)

class AddViewModel(
    private val appId: Long,
    private val repo: WebAppRepository,
    private val saveWebApp: SaveWebAppUseCase,
    getCategories: GetCategoriesUseCase,
    private val analyzer: PwaAnalyzer,
    private val faviconFetcher: FaviconFetcher,
) : ViewModel() {

    private val _state = MutableStateFlow(AddUiState(isLoading = appId != 0L))
    val uiState: StateFlow<AddUiState> = _state

    private var originalApp: WebApp? = null

    init {
        if (appId != 0L) {
            viewModelScope.launch {
                val app = repo.getById(appId) ?: return@launch
                originalApp = app
                _state.update {
                    it.copy(
                        isLoading = false,
                        name = app.name,
                        url = app.url,
                        iconPath = app.iconPath,
                        themeColor = app.themeColor,
                        categoryId = app.categoryId,
                        isolationId = app.isolationId,
                        isFullscreen = app.isFullscreen,
                        fullscreenShowStatusBar = app.fullscreenShowStatusBar,
                        fullscreenShowNavBar = app.fullscreenShowNavBar,
                        fullscreenShowTopToolbar = app.fullscreenShowTopToolbar,
                        adBlockEnabled = app.adBlockEnabled,
                        adBlockAllowUserToggle = app.adBlockAllowUserToggle,
                        adBlockCustomRules = app.adBlockCustomRules,
                        translateEnabled = app.translateEnabled,
                        translateTarget = app.translateTarget,
                        translateEngine = app.translateEngine,
                        showTranslateButton = app.showTranslateButton,
                        autoTranslateOnLoad = app.autoTranslateOnLoad,
                        uaMode = app.uaMode,
                    )
                }
            }
        }
    }

    // Basic info
    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setUrl(v: String) = _state.update { it.copy(url = v, urlError = null) }
    fun setThemeColor(v: String?) = _state.update { it.copy(themeColor = v) }
    fun setIconPath(v: String) = _state.update { it.copy(iconPath = v) }

    // Fullscreen
    fun setFullscreen(v: Boolean) = _state.update { it.copy(isFullscreen = v) }
    fun setFullscreenShowStatusBar(v: Boolean) = _state.update { it.copy(fullscreenShowStatusBar = v) }
    fun setFullscreenShowNavBar(v: Boolean) = _state.update { it.copy(fullscreenShowNavBar = v) }
    fun setFullscreenShowTopToolbar(v: Boolean) = _state.update { it.copy(fullscreenShowTopToolbar = v) }

    // Ad blocking
    fun setAdBlock(v: Boolean) = _state.update { it.copy(adBlockEnabled = v) }
    fun setAdBlockAllowUserToggle(v: Boolean) = _state.update { it.copy(adBlockAllowUserToggle = v) }
    fun setAdBlockCustomRuleInput(v: String) = _state.update { it.copy(adBlockCustomRuleInput = v) }
    fun addAdBlockCustomRule() {
        val rule = _state.value.adBlockCustomRuleInput.trim()
        if (rule.isBlank() || rule in _state.value.adBlockCustomRules) return
        _state.update { it.copy(adBlockCustomRules = it.adBlockCustomRules + rule, adBlockCustomRuleInput = "") }
    }
    fun removeAdBlockCustomRule(rule: String) = _state.update { it.copy(adBlockCustomRules = it.adBlockCustomRules - rule) }

    // Translation
    fun setTranslate(v: Boolean) = _state.update { it.copy(translateEnabled = v) }
    fun setTranslateTarget(v: TranslateLanguage) = _state.update { it.copy(translateTarget = v) }
    fun setTranslateEngine(v: TranslateEngine) = _state.update { it.copy(translateEngine = v) }
    fun setShowTranslateButton(v: Boolean) = _state.update { it.copy(showTranslateButton = v) }
    fun setAutoTranslateOnLoad(v: Boolean) = _state.update { it.copy(autoTranslateOnLoad = v) }

    // Browser
    fun setUaMode(v: UserAgentMode) = _state.update { it.copy(uaMode = v) }

    // ── Analysis ──────────────────────────────────────────────────────────────

    fun analyze() {
        val rawUrl = _state.value.url.trim().let { if (!it.startsWith("http")) "https://$it" else it }
        if (rawUrl.isBlank()) { _state.update { it.copy(urlError = "Please enter a URL") }; return }
        _state.update { it.copy(isAnalyzing = true, analyzeError = null, url = rawUrl) }
        viewModelScope.launch {
            runCatching {
                val manifest = analyzer.analyze(rawUrl)
                val iconUrl = manifest.bestIconUrl(rawUrl)
                val iconPath = if (iconUrl != null) {
                    faviconFetcher.fetch(iconUrl, rawUrl, _state.value.isolationId)
                } else null
                _state.update { it.copy(isAnalyzing = false, pendingManifest = manifest, pendingIconPath = iconPath) }
            }.onFailure {
                _state.update { it.copy(isAnalyzing = false, analyzeError = "Could not read site info. You can still save manually.") }
            }
        }
    }

    /** Apply all detected fields from the analysis report to the form. */
    fun applyManifest() {
        val s = _state.value
        val m = s.pendingManifest ?: return
        _state.update {
            it.copy(
                name = it.name.ifBlank { m.shortName ?: m.name ?: it.name },
                themeColor = m.themeColor ?: it.themeColor,
                iconPath = s.pendingIconPath ?: it.iconPath,
                pendingManifest = null,
                pendingIconPath = null,
            )
        }
    }

    fun dismissManifest() = _state.update { it.copy(pendingManifest = null, pendingIconPath = null) }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun save(onCreateShortcut: ((WebApp) -> Unit)? = null) {
        val s = _state.value
        val url = s.url.trim().let { if (!it.startsWith("http")) "https://$it" else it }
        if (url.isBlank()) { _state.update { it.copy(urlError = "Please enter a URL") }; return }
        if (s.name.isBlank()) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val app = (originalApp ?: WebApp(name = "", url = "", isolationId = s.isolationId)).copy(
                id = appId,
                name = s.name.trim(),
                url = url,
                iconPath = s.iconPath,
                themeColor = s.themeColor,
                categoryId = s.categoryId,
                isolationId = s.isolationId,
                isFullscreen = s.isFullscreen,
                fullscreenShowStatusBar = s.fullscreenShowStatusBar,
                fullscreenShowNavBar = s.fullscreenShowNavBar,
                fullscreenShowTopToolbar = s.fullscreenShowTopToolbar,
                adBlockEnabled = s.adBlockEnabled,
                adBlockAllowUserToggle = s.adBlockAllowUserToggle,
                adBlockCustomRules = s.adBlockCustomRules,
                translateEnabled = s.translateEnabled,
                translateTarget = s.translateTarget,
                translateEngine = s.translateEngine,
                showTranslateButton = s.showTranslateButton,
                autoTranslateOnLoad = s.autoTranslateOnLoad,
                uaMode = s.uaMode,
            )
            val savedId = saveWebApp(app)
            val savedApp = repo.getById(savedId) ?: app.copy(id = savedId)
            onCreateShortcut?.invoke(savedApp)
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
