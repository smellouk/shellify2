package io.shellify.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.shellify.app.core.engine.GeckoEngineManager
import io.shellify.core.ui.R as CoreUiR
import io.shellify.app.core.engine.GeckoInstallState
import io.shellify.app.core.security.PasswordManager
import io.shellify.app.core.security.hashPassword
import io.shellify.app.core.security.verifyPassword
import io.shellify.app.domain.model.LockType
import io.shellify.app.presentation.settings.AppSettingsScreen
import io.shellify.app.presentation.settings.AppSettingsUiState
import io.shellify.app.presentation.settings.AppSettingsViewModel
import io.shellify.app.presentation.theme.ShellifyTheme
import io.shellify.app.util.FakeData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E smoke tests for protected apps.
 *
 * Covers two layers:
 *  1. Pure password hashing/verification (no Android Context needed)
 *  2. PasswordManager DataStore integration (set, verify, clear lifecycle)
 *  3. AppSettingsScreen UI: lock toggle, lock type selector, disable-lock dialog
 */
@RunWith(AndroidJUnit4::class)
class SmokeProtectedAppTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    // ── Pure password hashing ─────────────────────────────────────────────────

    @Test
    fun hashAndVerify_correctPassword_returnsTrue() {
        val hash = hashPassword("secure-pass-42")
        assertTrue(verifyPassword("secure-pass-42", hash))
    }

    @Test
    fun hashAndVerify_wrongPassword_returnsFalse() {
        val hash = hashPassword("secure-pass-42")
        assertFalse(verifyPassword("wrong-password", hash))
    }

    @Test
    fun hashPassword_sameInput_producesDifferentSaltedHashes() {
        val hash1 = hashPassword("same-password")
        val hash2 = hashPassword("same-password")
        // Salted PBKDF2 — same input must never produce the same hash
        assertNotEquals(hash1, hash2)
        assertTrue(verifyPassword("same-password", hash1))
        assertTrue(verifyPassword("same-password", hash2))
    }

    // ── PasswordManager DataStore integration ─────────────────────────────────

    @Test
    fun passwordManager_setPassword_hashIsStoredAndVerifiable() = runBlocking {
        val pm = PasswordManager(context)
        try {
            pm.setPassword("integration-test-pw")
            val stored = pm.passwordHash.first()
            assertNotNull("Hash should be stored after setPassword", stored)
            assertTrue(verifyPassword("integration-test-pw", stored!!))
            assertFalse(verifyPassword("wrong-pw", stored))
        } finally {
            pm.clearPassword()
        }
    }

    @Test
    fun passwordManager_clearPassword_removesHash() = runBlocking {
        val pm = PasswordManager(context)
        pm.setPassword("temp-pw")
        pm.clearPassword()
        assertNull("Hash should be null after clearPassword", pm.passwordHash.first())
    }

    @Test
    fun passwordManager_failedAttempts_incrementsAndClearsCorrectly() = runBlocking {
        val pm = PasswordManager(context)
        val appId = 9999L
        try {
            pm.clearFailedAttempts(appId)
            val first = pm.recordFailedAttempt(appId)
            val second = pm.recordFailedAttempt(appId)
            assertTrue("Attempts should increment", second > first)
            pm.clearFailedAttempts(appId)
            val afterClear = pm.getFailedAttempts(appId)
            assertTrue("Attempts should be 0 after clear", afterClear == 0)
        } finally {
            pm.clearFailedAttempts(appId)
        }
    }

    // ── AppSettingsScreen: lock toggle UI ─────────────────────────────────────

    @Test
    fun lockToggle_isVisibleWhenAppIsLoaded() {
        val app = FakeData.webApp(id = 1L, name = "Notion")
        setScreen(AppSettingsUiState(app = app, isLoading = false))
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.settings_applock)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun lockSection_showsLockTypeSelector_whenPasswordLockEnabled() {
        val app = FakeData.webApp(id = 1L, name = "Notion").copy(lockType = LockType.PASSWORD)
        setScreen(AppSettingsUiState(app = app, isLoading = false))
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.settings_lock_password), substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(CoreUiR.string.settings_lock_system), substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    // ── AppSettingsScreen: disable lock dialog ────────────────────────────────

    @Test
    fun disableLockDialog_isShownWhenFlagSet() {
        val app = FakeData.webApp(id = 1L).copy(lockType = LockType.PASSWORD)
        setScreen(AppSettingsUiState(app = app, isLoading = false, showDisableLockDialog = true))
        // "Disable App Lock" appears as both dialog title and confirm button — verify at least one
        composeTestRule.onAllNodesWithText(context.getString(CoreUiR.string.settings_disable_lock_title))[0].assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.common_current_password)).assertIsDisplayed()
    }

    @Test
    fun disableLockDialog_wrongPassword_showsErrorMessage() {
        val app = FakeData.webApp(id = 1L).copy(lockType = LockType.PASSWORD)
        setScreen(
            AppSettingsUiState(
                app = app,
                isLoading = false,
                showDisableLockDialog = true,
                disableLockError = true,
            )
        )
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.common_wrong_password)).assertIsDisplayed()
    }

    @Test
    fun disableLockDialog_cancelButton_isVisible() {
        val app = FakeData.webApp(id = 1L).copy(lockType = LockType.PASSWORD)
        setScreen(AppSettingsUiState(app = app, isLoading = false, showDisableLockDialog = true))
        composeTestRule.onNodeWithText(context.getString(CoreUiR.string.common_cancel)).assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun setScreen(state: AppSettingsUiState) {
        val geckoManager = mockk<GeckoEngineManager>(relaxed = true)
        every { geckoManager.installState } returns MutableStateFlow(GeckoInstallState.NotInstalled)
        val vm = mockk<AppSettingsViewModel>(relaxed = true)
        every { vm.uiState } returns MutableStateFlow(state)
        every { vm.geckoEngineManager } returns geckoManager
        every { vm.commands } returns MutableSharedFlow()
        composeTestRule.setContent {
            ShellifyTheme {
                AppSettingsScreen(viewModel = vm, onBack = {}, onDeleted = {})
            }
        }
    }
}
