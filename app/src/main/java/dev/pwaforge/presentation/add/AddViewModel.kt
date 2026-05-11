package dev.pwaforge.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.core.pwa.FaviconFetcher
import dev.pwaforge.core.pwa.PwaAnalyzer
import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.model.TranslateLanguage
import dev.pwaforge.domain.model.UserAgentMode
import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.repository.WebAppRepository
import dev.pwaforge.domain.usecase.GetCategoriesUseCase
import dev.pwaforge.domain.usecase.SaveWebAppUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddUiState(
    val isLoading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val url: String = "",
    val iconPath: String? = null,
    val themeColor: String? = null,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isFullscreen: Boolean = false,
    val adBlockEnabled: Boolean = true,
    val translateEnabled: Boolean = false,
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

    val categories: StateFlow<List<Category>> = getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Preserved for delta edits (isolation ID must not change on edit)
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
                        isFullscreen = app.isFullscreen,
                        adBlockEnabled = app.adBlockEnabled,
                        translateEnabled = app.translateEnabled,
                        uaMode = app.uaMode,
                    )
                }
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setUrl(v: String) = _state.update { it.copy(url = v, urlError = null) }
    fun setCategory(id: Long?) = _state.update { it.copy(categoryId = id) }
    fun setFullscreen(v: Boolean) = _state.update { it.copy(isFullscreen = v) }
    fun setAdBlock(v: Boolean) = _state.update { it.copy(adBlockEnabled = v) }
    fun setTranslate(v: Boolean) = _state.update { it.copy(translateEnabled = v) }
    fun setUaMode(v: UserAgentMode) = _state.update { it.copy(uaMode = v) }

    fun analyze() {
        val rawUrl = _state.value.url.trim().let { if (!it.startsWith("http")) "https://$it" else it }
        if (rawUrl.isBlank()) { _state.update { it.copy(urlError = "Please enter a URL") }; return }
        _state.update { it.copy(isAnalyzing = true, analyzeError = null, url = rawUrl) }
        viewModelScope.launch {
            runCatching {
                val manifest = analyzer.analyze(rawUrl)
                val isolationId = originalApp?.isolationId ?: WebApp(name = "", url = "").isolationId
                val iconPath = faviconFetcher.fetch(manifest.bestIconUrl(rawUrl), rawUrl, isolationId)
                _state.update { s ->
                    s.copy(
                        isAnalyzing = false,
                        name = s.name.ifBlank { manifest.shortName ?: manifest.name ?: "" },
                        themeColor = manifest.themeColor,
                        iconPath = iconPath ?: s.iconPath,
                    )
                }
            }.onFailure {
                _state.update { s -> s.copy(isAnalyzing = false, analyzeError = "Could not read site info. You can still save manually.") }
            }
        }
    }

    fun save(onCreateShortcut: ((WebApp) -> Unit)? = null) {
        val s = _state.value
        val url = s.url.trim().let { if (!it.startsWith("http")) "https://$it" else it }
        if (url.isBlank()) { _state.update { it.copy(urlError = "Please enter a URL") }; return }
        if (s.name.isBlank()) { return }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val app = (originalApp ?: WebApp(name = "", url = "")).copy(
                id = appId,
                name = s.name.trim(),
                url = url,
                iconPath = s.iconPath,
                themeColor = s.themeColor,
                categoryId = s.categoryId,
                isFullscreen = s.isFullscreen,
                adBlockEnabled = s.adBlockEnabled,
                translateEnabled = s.translateEnabled,
                uaMode = s.uaMode,
            )
            val savedId = saveWebApp(app)
            val savedApp = repo.getById(savedId) ?: app.copy(id = savedId)
            onCreateShortcut?.invoke(savedApp)
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
