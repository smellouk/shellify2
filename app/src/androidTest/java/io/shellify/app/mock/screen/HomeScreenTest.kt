package io.shellify.app.mock.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.verify
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.shellify.app.domain.model.Category
import io.shellify.app.domain.model.WebApp
import io.shellify.app.presentation.home.HomeScreen
import io.shellify.app.presentation.home.HomeUiState
import io.shellify.app.presentation.home.HomeViewModel
import io.shellify.app.presentation.theme.ShellifyTheme
import io.shellify.app.util.FakeData
import io.shellify.core.ui.R as CoreUiR
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for HomeScreen.
 *
 * ViewModels are mocked via MockK and a [MutableStateFlow] is used to
 * drive different UI states without touching the database or DI chain.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun buildViewModel(uiState: HomeUiState): HomeViewModel {
        val vm = mockk<HomeViewModel>(relaxed = true)
        every { vm.uiState } returns MutableStateFlow(uiState)
        return vm
    }

    private fun setHomeScreen(
        uiState: HomeUiState,
        geckoInstalled: Boolean = true,
    ) {
        composeTestRule.setContent {
            ShellifyTheme {
                HomeScreen(
                    viewModel = buildViewModel(uiState),
                    geckoInstalled = geckoInstalled,
                    currentLanguage = "en",
                    onLanguageChange = {},
                    onAddApp = {},
                    onEditApp = {},
                    onOpenApp = {},
                    onOpenSettings = {},
                )
            }
        }
    }

    // ─── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun emptyState_showsForgeYourFirstAppTitle() {
        setHomeScreen(HomeUiState(apps = emptyList(), isLoading = false))
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.home_empty_title))
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsAddWebsiteButton() {
        setHomeScreen(HomeUiState(apps = emptyList(), isLoading = false))
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.home_empty_subtitle_action))
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsQuickSuggestions() {
        setHomeScreen(HomeUiState(apps = emptyList(), isLoading = false))
        composeTestRule
            .onNodeWithText("Reddit")
            .assertIsDisplayed()
    }

    // ─── Apps list ─────────────────────────────────────────────────────────────

    @Test
    fun appList_displaysSingleAppName() {
        val app = FakeData.webApp(id = 1L, name = "My App", url = "https://myapp.com")
        setHomeScreen(
            HomeUiState(apps = listOf(app), hasAnyApps = true, isLoading = false)
        )
        composeTestRule
            .onNodeWithText("My App")
            .assertIsDisplayed()
    }

    @Test
    fun appList_displaysMultipleAppNames() {
        val apps = listOf(
            FakeData.webApp(id = 1L, name = "Alpha App"),
            FakeData.webApp(id = 2L, name = "Beta App"),
            FakeData.webApp(id = 3L, name = "Gamma App"),
        )
        setHomeScreen(HomeUiState(apps = apps, hasAnyApps = true, isLoading = false))

        composeTestRule.onNodeWithText("Alpha App").assertIsDisplayed()
        composeTestRule.onNodeWithText("Beta App").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gamma App").assertIsDisplayed()
    }

    @Test
    fun appList_showsSearchHintWhenAppsPresent() {
        val apps = listOf(FakeData.webApp(id = 1L, name = "Some App"))
        setHomeScreen(HomeUiState(apps = apps, hasAnyApps = true, isLoading = false))
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.home_search_hint))
            .assertIsDisplayed()
    }

    // ─── FAB ──────────────────────────────────────────────────────────────────

    @Test
    fun fab_isDisplayedWhenAppsListIsNonEmpty() {
        val apps = listOf(FakeData.webApp(id = 1L, name = "SomeApp"))
        setHomeScreen(HomeUiState(apps = apps, hasAnyApps = true, isLoading = false))
        composeTestRule
            .onNodeWithContentDescription(context.getString(CoreUiR.string.home_add_fab_cd))
            .assertIsDisplayed()
    }

    @Test
    fun fab_isNotDisplayedWhenAppsListIsEmpty() {
        setHomeScreen(HomeUiState(apps = emptyList(), hasAnyApps = false, isLoading = false))
        composeTestRule
            .onNodeWithContentDescription(context.getString(CoreUiR.string.home_add_fab_cd))
            .assertDoesNotExist()
    }

    // ─── Category filter chips ─────────────────────────────────────────────────

    @Test
    fun categoryFilterChips_showAllChipAndCategoryNames() {
        val apps = listOf(FakeData.webApp(id = 1L, name = "App", categoryId = 1L))
        val categories = listOf(FakeData.category(id = 1L, name = "Work"))
        setHomeScreen(
            HomeUiState(
                apps = apps,
                hasAnyApps = true,
                categories = categories,
                isLoading = false,
            )
        )
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_all_categories)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }

    // ─── Top bar ──────────────────────────────────────────────────────────────

    @Test
    fun topBar_showsShellifyTitle() {
        setHomeScreen(HomeUiState(isLoading = false))
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.home_title))
            .assertIsDisplayed()
    }

    @Test
    fun topBar_showsLanguageChangeButton() {
        setHomeScreen(HomeUiState(isLoading = false))
        composeTestRule
            .onNodeWithContentDescription(context.getString(CoreUiR.string.language_change_cd))
            .assertIsDisplayed()
    }

    // ─── Empty filtered category state ────────────────────────────────────────

    @Test
    fun filteredEmptyState_showsEmptyFilteredCategoryMessage() {
        val category = FakeData.category(id = 5L, name = "Media")
        setHomeScreen(
            HomeUiState(
                apps = emptyList(),
                hasAnyApps = true,
                categories = listOf(category),
                selectedCategoryId = 5L,
                isLoading = false,
            )
        )
        // The screen shows "No apps in 'Media'" when filtered category is empty
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.home_empty_filtered_category, "Media"), substring = true)
            .assertIsDisplayed()
    }

    // ─── Context menu ─────────────────────────────────────────────────────────

    @Test
    fun contextMenu_showsAllItems() {
        val app = FakeData.webApp(id = 1L, name = "GitHub", url = "https://github.com")
        setHomeScreen(HomeUiState(apps = listOf(app), hasAnyApps = true, isLoading = false))

        composeTestRule.onNodeWithContentDescription(context.getString(CoreUiR.string.home_menu_more_cd)).performClick()

        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_menu_assign_category)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.share_button)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_menu_clear_data)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_menu_delete)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_menu_settings)).assertIsDisplayed()
    }

    @Test
    fun contextMenu_deleteItem_showsConfirmationDialog() {
        val app = FakeData.webApp(id = 1L, name = "GitHub", url = "https://github.com")
        setHomeScreen(HomeUiState(apps = listOf(app), hasAnyApps = true, isLoading = false))

        composeTestRule.onNodeWithContentDescription(context.getString(CoreUiR.string.home_menu_more_cd)).performClick()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_menu_delete)).performClick()

        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.settings_delete_confirm, "GitHub"))
            .assertIsDisplayed()
    }

    @Test
    fun contextMenu_deleteConfirmed_callsViewModelDelete() {
        val app = FakeData.webApp(id = 1L, name = "GitHub", url = "https://github.com")
        val vm = buildViewModel(HomeUiState(apps = listOf(app), hasAnyApps = true, isLoading = false))
        composeTestRule.setContent {
            ShellifyTheme {
                HomeScreen(
                    viewModel = vm,
                    geckoInstalled = true,
                    currentLanguage = "en",
                    onLanguageChange = {},
                    onAddApp = {},
                    onEditApp = {},
                    onOpenApp = {},
                    onOpenSettings = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(context.getString(CoreUiR.string.home_menu_more_cd)).performClick()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_menu_delete)).performClick()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.home_menu_delete)).performClick()

        verify(exactly = 1) { vm.delete(app) }
    }
}
