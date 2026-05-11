package dev.pwaforge.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.domain.usecase.DeleteWebAppUseCase
import dev.pwaforge.domain.usecase.GetCategoriesUseCase
import dev.pwaforge.domain.usecase.GetWebAppsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val apps: List<WebApp> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
)

class HomeViewModel(
    getWebApps: GetWebAppsUseCase,
    private val deleteWebApp: DeleteWebAppUseCase,
    getCategories: GetCategoriesUseCase,
) : ViewModel() {

    private val _extra = MutableStateFlow(Pair<Long?, String>(null, ""))

    val uiState: StateFlow<HomeUiState> = combine(
        getWebApps(),
        getCategories(),
        _extra,
    ) { apps, categories, (selectedCategory, query) ->
        val filtered = apps
            .filter { if (selectedCategory != null) it.categoryId == selectedCategory else true }
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
        HomeUiState(
            apps = filtered,
            categories = categories,
            selectedCategoryId = selectedCategory,
            searchQuery = query,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun selectCategory(id: Long?) = _extra.update { it.copy(first = id) }
    fun setSearch(query: String) = _extra.update { it.copy(second = query) }

    fun delete(app: WebApp) = viewModelScope.launch { deleteWebApp(app) }
}
