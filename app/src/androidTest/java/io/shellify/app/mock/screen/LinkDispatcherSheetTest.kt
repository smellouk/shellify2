package io.shellify.app.mock.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.shellify.app.domain.model.WebApp
import io.shellify.app.presentation.linkdispatcher.DispatchSheet
import io.shellify.app.presentation.linkdispatcher.LinkDispatcherSheet
import io.shellify.app.presentation.linkdispatcher.LinkDispatcherUiState
import io.shellify.app.presentation.theme.ShellifyTheme
import io.shellify.app.util.FakeData
import io.shellify.core.ui.R as CoreUiR
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkDispatcherSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setSheet(
        uiState: LinkDispatcherUiState,
        onAppSelected: (WebApp, String) -> Unit = { _, _ -> },
        onDismiss: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            ShellifyTheme {
                LinkDispatcherSheet(
                    uiState = uiState,
                    onAppSelected = onAppSelected,
                    onDismiss = onDismiss,
                )
            }
        }
    }

    // ── None state ────────────────────────────────────────────────────────────

    @Test
    fun noneState_doesNotShowChooserTitle() {
        setSheet(LinkDispatcherUiState(sheet = DispatchSheet.None))
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.link_dispatcher_chooser_title))
            .assertDoesNotExist()
    }

    // ── Chooser state ─────────────────────────────────────────────────────────

    @Test
    fun chooserState_showsTitle() {
        val app = FakeData.webApp(name = "GitHub", url = "https://github.com")
        setSheet(LinkDispatcherUiState(sheet = DispatchSheet.Chooser(listOf(app), "https://github.com")))
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.link_dispatcher_chooser_title))
            .assertIsDisplayed()
    }

    @Test
    fun chooserState_showsAllAppNames() {
        val apps = listOf(
            FakeData.webApp(name = "GitHub", url = "https://github.com"),
            FakeData.webApp(name = "GitLab", url = "https://gitlab.com"),
        )
        setSheet(LinkDispatcherUiState(sheet = DispatchSheet.Chooser(apps, "https://github.com")))
        composeTestRule.onNodeWithText("GitHub").assertIsDisplayed()
        composeTestRule.onNodeWithText("GitLab").assertIsDisplayed()
    }

    @Test
    fun chooserState_showsAppHost() {
        val app = FakeData.webApp(name = "GitHub", url = "https://github.com")
        setSheet(LinkDispatcherUiState(sheet = DispatchSheet.Chooser(listOf(app), "https://github.com")))
        composeTestRule.onNodeWithText("github.com", substring = true).assertIsDisplayed()
    }

    @Test
    fun chooserState_tappingApp_invokesOnAppSelectedWithCorrectUrlAndApp() {
        val app = FakeData.webApp(name = "GitHub", url = "https://github.com")
        var selectedApp: WebApp? = null
        var selectedUrl: String? = null
        setSheet(
            uiState = LinkDispatcherUiState(
                sheet = DispatchSheet.Chooser(listOf(app), "https://github.com/page"),
            ),
            onAppSelected = { a, u -> selectedApp = a; selectedUrl = u },
        )
        composeTestRule.onNodeWithText("GitHub").performClick()
        assertEquals(app, selectedApp)
        assertEquals("https://github.com/page", selectedUrl)
    }

    @Test
    fun chooserState_backPress_invokesOnDismiss() {
        val app = FakeData.webApp(name = "GitHub", url = "https://github.com")
        var dismissed = false
        setSheet(
            uiState = LinkDispatcherUiState(
                sheet = DispatchSheet.Chooser(listOf(app), "https://github.com"),
            ),
            onDismiss = { dismissed = true },
        )
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.link_dispatcher_chooser_title))
            .assertIsDisplayed()
        androidx.test.espresso.Espresso.pressBack()
        assertEquals(true, dismissed)
    }
}
