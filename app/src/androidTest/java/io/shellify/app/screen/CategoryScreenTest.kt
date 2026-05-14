package io.shellify.app.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.domain.model.Category
import io.shellify.app.presentation.category.CategoryScreen
import io.shellify.app.presentation.category.CategoryUiState
import io.shellify.app.presentation.category.CategoryViewModel
import io.shellify.app.presentation.theme.ShellifyTheme
import io.shellify.app.util.FakeData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for CategoryScreen.
 *
 * Uses a mocked CategoryViewModel so we can drive the loading / empty / populated
 * states without touching Room or use-cases.
 */
@RunWith(AndroidJUnit4::class)
class CategoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildViewModel(
        categories: List<Category>?,
        uiState: CategoryUiState = CategoryUiState(),
    ): CategoryViewModel {
        val vm = mockk<CategoryViewModel>(relaxed = true)
        every { vm.categories } returns MutableStateFlow(categories)
        every { vm.uiState } returns MutableStateFlow(uiState)
        return vm
    }

    private fun setCategoryScreen(
        categories: List<Category>?,
        uiState: CategoryUiState = CategoryUiState(),
    ) {
        composeTestRule.setContent {
            ShellifyTheme {
                CategoryScreen(viewModel = buildViewModel(categories, uiState))
            }
        }
    }

    // ─── Top bar ──────────────────────────────────────────────────────────────

    @Test
    fun topBar_showsCategoriesTitle() {
        setCategoryScreen(categories = emptyList())
        composeTestRule
            .onNodeWithText("Categories")
            .assertIsDisplayed()
    }

    // ─── Empty state ──────────────────────────────────────────────────────────

    @Test
    fun emptyState_showsSortAppsTitle() {
        setCategoryScreen(categories = emptyList())
        composeTestRule
            .onNodeWithText("Sort apps into spaces")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptySubtitle() {
        setCategoryScreen(categories = emptyList())
        composeTestRule
            .onNodeWithText("Group your tiles by purpose", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsSuggestionChips() {
        setCategoryScreen(categories = emptyList())
        // Quick-add suggestion chips from strings.xml
        composeTestRule.onNodeWithText("Media").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }

    // ─── Populated list ────────────────────────────────────────────────────────

    @Test
    fun populatedList_displaysCategoryName() {
        val categories = listOf(FakeData.category(id = 1L, name = "Social"))
        setCategoryScreen(categories = categories)
        composeTestRule
            .onNodeWithText("Social")
            .assertIsDisplayed()
    }

    @Test
    fun populatedList_displaysMultipleCategoryNames() {
        val categories = listOf(
            FakeData.category(id = 1L, name = "Entertainment"),
            FakeData.category(id = 2L, name = "Productivity"),
        )
        setCategoryScreen(categories = categories)
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
    }

    @Test
    fun populatedList_showsFabAddCategory() {
        val categories = listOf(FakeData.category(id = 1L, name = "Work"))
        setCategoryScreen(categories = categories)
        composeTestRule
            .onNodeWithContentDescription("Add category")
            .assertIsDisplayed()
    }

    // ─── Add category dialog ─────────────────────────────────────────────────

    @Test
    fun addDialog_isDisplayedWhenShowAddDialogIsTrue() {
        setCategoryScreen(
            categories = emptyList(),
            uiState = CategoryUiState(showAddDialog = true),
        )
        composeTestRule
            .onNodeWithText("New Category")
            .assertIsDisplayed()
    }

    @Test
    fun editDialog_isDisplayedWhenEditingIdIsSet() {
        val category = FakeData.category(id = 3L, name = "Reading")
        setCategoryScreen(
            categories = listOf(category),
            uiState = CategoryUiState(
                showAddDialog = true,
                editingId = 3L,
                newName = "Reading",
            ),
        )
        composeTestRule
            .onNodeWithText("Edit Category")
            .assertIsDisplayed()
    }
}
