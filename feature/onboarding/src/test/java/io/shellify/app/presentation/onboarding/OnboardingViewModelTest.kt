package io.shellify.app.presentation.onboarding

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.shellify.app.core.backup.BackupSchedule
import io.shellify.app.core.backup.BackupSettings
import io.shellify.app.core.pwa.FaviconFetcher
import io.shellify.app.core.pwa.PwaAnalyzer
import io.shellify.app.core.security.PasswordManager
import io.shellify.app.core.theme.ThemeManager
import io.shellify.app.core.theme.ThemeMode
import io.shellify.app.domain.usecase.SaveWebAppUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val themeManager = mockk<ThemeManager>(relaxed = true)
    private val passwordManager = mockk<PasswordManager>(relaxed = true)
    private val backupSettings = mockk<BackupSettings>(relaxed = true)
    private val saveWebApp = mockk<SaveWebAppUseCase>(relaxed = true)
    private val pwaAnalyzer = mockk<PwaAnalyzer>(relaxed = true)
    private val faviconFetcher = mockk<FaviconFetcher>(relaxed = true)
    private var finishedCalled = false

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { themeManager.onboardingPage } returns MutableStateFlow(0)
        every { themeManager.themeMode } returns MutableStateFlow(ThemeMode.SYSTEM)
        every { themeManager.accentColor } returns MutableStateFlow(null)
        every { passwordManager.passwordHash } returns MutableStateFlow(null)
        every { backupSettings.enabled } returns MutableStateFlow(false)
        every { backupSettings.directoryUri } returns MutableStateFlow(null)
        every { backupSettings.schedule } returns MutableStateFlow(BackupSchedule.NONE)
        finishedCalled = false
        viewModel = OnboardingViewModel(
            themeManager = themeManager,
            passwordManager = passwordManager,
            backupSettings = backupSettings,
            saveWebApp = saveWebApp,
            pwaAnalyzer = pwaAnalyzer,
            faviconFetcher = faviconFetcher,
            onFinished = { finishedCalled = true },
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has page 0 and default theme`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(0, state.page)
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertFalse(state.passwordSet)
        assertFalse(state.backupEnabled)
    }

    @Test
    fun `goTo updates page and persists via themeManager`() = runTest {
        viewModel.goTo(2)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.page)
        coVerify(exactly = 1) { themeManager.saveOnboardingPage(2) }
    }

    @Test
    fun `setThemeMode updates state and persists`() = runTest {
        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
        coVerify(exactly = 1) { themeManager.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `setAccentColor updates state and persists`() = runTest {
        viewModel.setAccentColor(0xFF0000)
        advanceUntilIdle()
        assertEquals(0xFF0000, viewModel.uiState.value.accentColor)
        coVerify(exactly = 1) { themeManager.setAccentColor(0xFF0000) }
    }

    @Test
    fun `setPassword marks passwordSet true`() = runTest {
        viewModel.setPassword("secret123")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.passwordSet)
        coVerify(exactly = 1) { passwordManager.setPassword("secret123") }
    }

    @Test
    fun `setBackupEnabled updates state and persists`() = runTest {
        viewModel.setBackupEnabled(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.backupEnabled)
        coVerify(exactly = 1) { backupSettings.setEnabled(true) }
    }

    @Test
    fun `setBackupDirectoryUri updates state and persists`() = runTest {
        viewModel.setBackupDirectoryUri("content://backup/dir")
        advanceUntilIdle()
        assertEquals("content://backup/dir", viewModel.uiState.value.backupDirectoryUri)
        coVerify(exactly = 1) { backupSettings.setDirectoryUri("content://backup/dir") }
    }

    @Test
    fun `togglePickedApp adds app id when not present`() {
        viewModel.togglePickedApp("app-1")
        assertTrue("app-1" in viewModel.uiState.value.pickedAppIds)
    }

    @Test
    fun `togglePickedApp removes app id when already present`() {
        viewModel.togglePickedApp("app-1")
        viewModel.togglePickedApp("app-1")
        assertFalse("app-1" in viewModel.uiState.value.pickedAppIds)
    }

    @Test
    fun `cancelQuickPicks resets status to Idle`() = runTest {
        viewModel.cancelQuickPicks()
        assertEquals(QuickPicksStatus.Idle, viewModel.uiState.value.quickPicksStatus)
    }

    @Test
    fun `finish calls onFinished callback`() = runTest {
        viewModel.finish()
        advanceUntilIdle()
        assertTrue(finishedCalled)
        coVerify(exactly = 1) { themeManager.setOnboardingDone() }
    }
}
