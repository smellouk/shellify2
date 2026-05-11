package dev.pwaforge.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.repository.CategoryRepository
import dev.pwaforge.domain.usecase.GetCategoriesUseCase
import dev.pwaforge.domain.usecase.SaveCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val newName: String = "",
    val editingId: Long? = null,
    val showAddDialog: Boolean = false,
    val selectedIcon: String = "folder",
    val selectedColor: String = "#6D28D9",
)

class CategoryViewModel(
    getCategories: GetCategoriesUseCase,
    private val saveCategory: SaveCategoryUseCase,
    private val repo: CategoryRepository,
) : ViewModel() {

    val categories = getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(CategoryUiState())
    val uiState = _state

    fun showDialog() = _state.update { it.copy(showAddDialog = true) }

    fun dismissDialog() = _state.update {
        it.copy(showAddDialog = false, newName = "", selectedIcon = "folder", selectedColor = "#6D28D9")
    }

    fun setNewName(name: String) = _state.update { it.copy(newName = name) }
    fun setSelectedIcon(icon: String) = _state.update { it.copy(selectedIcon = icon) }
    fun setSelectedColor(color: String) = _state.update { it.copy(selectedColor = color) }

    fun addCategory() {
        val name = _state.value.newName.trim().takeIf { it.isNotBlank() } ?: return
        val icon = _state.value.selectedIcon
        val color = _state.value.selectedColor
        viewModelScope.launch {
            saveCategory(Category(name = name, icon = icon, color = color))
            dismissDialog()
        }
    }

    fun delete(category: Category) = viewModelScope.launch { repo.delete(category) }
}
