package io.shellify.app.presentation.settings

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.shellify.app.core.engine.GeckoEngineManager
import io.shellify.app.core.iconpack.SimpleIconsManager
import io.shellify.app.core.iconpack.SimpleIconsState
import io.shellify.app.core.isolation.IsolationManager
import io.shellify.app.core.pwa.FaviconFetcher
import io.shellify.app.core.pwa.PwaAnalyzer
import io.shellify.app.core.security.PasswordManager
import io.shellify.app.core.theme.ThemeManager
import io.shellify.app.domain.model.NotificationPermission
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.usecase.DeleteWebAppUseCase
import io.shellify.app.domain.usecase.GetWebAppByIdUseCase
import io.shellify.app.domain.usecase.SaveWebAppUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
class AppSettingsViewModelNotificationTest {

    private val dispatcher = StandardTestDispatcher()

    private val getWebAppById = mockk<GetWebAppByIdUseCase>()
    private val saveWebApp = mockk<SaveWebAppUseCase>()
    private val deleteWebApp = mockk<DeleteWebAppUseCase>()
    private val isolationManager = mockk<IsolationManager>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val analyzer = mockk<PwaAnalyzer>(relaxed = true)
    private val faviconFetcher = mockk<FaviconFetcher>(relaxed = true)
    private val simpleIconsManager = mockk<SimpleIconsManager>(relaxed = true)
    private val passwordManager = mockk<PasswordManager>(relaxed = true)
    private val geckoEngineManager = mockk<GeckoEngineManager>(relaxed = true)
    private val themeManager = mockk<ThemeManager>(relaxed = true)

    private val testApp = WebApp(
        id = 42L,
        name = "TestApp",
        url = "https://test.com",
        isolationId = "iso-42",
        notificationPermission = NotificationPermission.NOT_ASKED,
        dndStartHour = -1,
        dndEndHour = -1,
        backgroundNotificationsEnabled = false,
    )

    private lateinit var viewModel: AppSettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { simpleIconsManager.state } returns MutableStateFlow(SimpleIconsState.NotImported)
        coEvery { getWebAppById(42L) } returns testApp
        coEvery { saveWebApp(any()) } returns 42L
        coEvery { deleteWebApp(any()) } returns Unit
        every { passwordManager.passwordHash } returns MutableStateFlow(null)
        every { themeManager.globalNotificationsEnabled } returns MutableStateFlow(true)
        viewModel = AppSettingsViewModel(
            appId = 42L,
            getWebAppById = getWebAppById,
            saveWebApp = saveWebApp,
            deleteWebApp = deleteWebApp,
            isolationManager = isolationManager,
            context = context,
            analyzer = analyzer,
            faviconFetcher = faviconFetcher,
            simpleIconsManager = simpleIconsManager,
            passwordManager = passwordManager,
            geckoEngineManager = geckoEngineManager,
            themeManager = themeManager,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleNotificationPermission fromNotAsked setsGranted andSaves`() = runTest {
        advanceUntilIdle()
        assertEquals(NotificationPermission.NOT_ASKED, viewModel.uiState.value.app?.notificationPermission)

        viewModel.toggleNotificationPermission()
        advanceUntilIdle()

        assertEquals(NotificationPermission.GRANTED, viewModel.uiState.value.app?.notificationPermission)
        coVerify(exactly = 1) { saveWebApp(match { it.notificationPermission == NotificationPermission.GRANTED }) }
    }

    @Test
    fun `toggleNotificationPermission fromGranted setsDenied`() = runTest {
        val grantedApp = testApp.copy(notificationPermission = NotificationPermission.GRANTED)
        coEvery { getWebAppById(42L) } returns grantedApp
        viewModel = AppSettingsViewModel(
            appId = 42L,
            getWebAppById = getWebAppById,
            saveWebApp = saveWebApp,
            deleteWebApp = deleteWebApp,
            isolationManager = isolationManager,
            context = context,
            analyzer = analyzer,
            faviconFetcher = faviconFetcher,
            simpleIconsManager = simpleIconsManager,
            passwordManager = passwordManager,
            geckoEngineManager = geckoEngineManager,
            themeManager = themeManager,
        )
        advanceUntilIdle()

        viewModel.toggleNotificationPermission()
        advanceUntilIdle()

        assertEquals(NotificationPermission.DENIED, viewModel.uiState.value.app?.notificationPermission)
    }

    @Test
    fun `setDndStartHour persistsHour`() = runTest {
        advanceUntilIdle()

        viewModel.setDndStartHour(7)
        advanceUntilIdle()

        assertEquals(7, viewModel.uiState.value.app?.dndStartHour)
        coVerify(exactly = 1) { saveWebApp(match { it.dndStartHour == 7 }) }
    }

    @Test
    fun `setDndEndHour minusOne clearsValue`() = runTest {
        advanceUntilIdle()

        viewModel.setDndEndHour(-1)
        advanceUntilIdle()

        assertEquals(-1, viewModel.uiState.value.app?.dndEndHour)
    }

    @Test
    fun `toggleBackgroundNotifications fromFalse emitsStartCommand`() = runTest {
        advanceUntilIdle()
        val commands = mutableListOf<AppSettingsCommand>()
        val job = launch { viewModel.commands.toList(commands) }

        viewModel.toggleBackgroundNotifications()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.app?.backgroundNotificationsEnabled ?: false)
        assertTrue(commands.any { it is AppSettingsCommand.StartBackgroundService && it.appId == 42L })

        job.cancel()
    }

    @Test
    fun `toggleBackgroundNotifications fromTrue emitsStopCommand`() = runTest {
        val enabledApp = testApp.copy(backgroundNotificationsEnabled = true)
        coEvery { getWebAppById(42L) } returns enabledApp
        viewModel = AppSettingsViewModel(
            appId = 42L,
            getWebAppById = getWebAppById,
            saveWebApp = saveWebApp,
            deleteWebApp = deleteWebApp,
            isolationManager = isolationManager,
            context = context,
            analyzer = analyzer,
            faviconFetcher = faviconFetcher,
            simpleIconsManager = simpleIconsManager,
            passwordManager = passwordManager,
            geckoEngineManager = geckoEngineManager,
            themeManager = themeManager,
        )
        advanceUntilIdle()
        val commands = mutableListOf<AppSettingsCommand>()
        val job = launch { viewModel.commands.toList(commands) }

        viewModel.toggleBackgroundNotifications()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.app?.backgroundNotificationsEnabled ?: true)
        assertTrue(commands.any { it is AppSettingsCommand.StopBackgroundService && it.appId == 42L })

        job.cancel()
    }

    @Test
    fun `onNotificationHistoryClick emitsNavigateCommand`() = runTest {
        advanceUntilIdle()
        val commands = mutableListOf<AppSettingsCommand>()
        val job = launch { viewModel.commands.toList(commands) }

        viewModel.onNotificationHistoryClick()
        advanceUntilIdle()

        assertTrue(commands.any { it is AppSettingsCommand.NavigateToNotificationHistory && it.appId == 42L })

        job.cancel()
    }

    @Test
    fun `clearDndSchedule resets both hours to minus one and saves`() = runTest {
        val appWithDnd = testApp.copy(dndStartHour = 22, dndEndHour = 8)
        coEvery { getWebAppById(42L) } returns appWithDnd
        viewModel = AppSettingsViewModel(
            appId = 42L,
            getWebAppById = getWebAppById,
            saveWebApp = saveWebApp,
            deleteWebApp = deleteWebApp,
            isolationManager = isolationManager,
            context = context,
            analyzer = analyzer,
            faviconFetcher = faviconFetcher,
            simpleIconsManager = simpleIconsManager,
            passwordManager = passwordManager,
            geckoEngineManager = geckoEngineManager,
            themeManager = themeManager,
        )
        advanceUntilIdle()
        assertEquals(22, viewModel.uiState.value.app?.dndStartHour)

        viewModel.clearDndSchedule()
        advanceUntilIdle()

        assertEquals(-1, viewModel.uiState.value.app?.dndStartHour)
        assertEquals(-1, viewModel.uiState.value.app?.dndEndHour)
        coVerify(exactly = 1) { saveWebApp(match { it.dndStartHour == -1 && it.dndEndHour == -1 }) }
    }

    @Test
    fun `globalNotificationsEnabled false reflects in state`() = runTest {
        val flow = MutableStateFlow(false)
        every { themeManager.globalNotificationsEnabled } returns flow
        viewModel = AppSettingsViewModel(
            appId = 42L,
            getWebAppById = getWebAppById,
            saveWebApp = saveWebApp,
            deleteWebApp = deleteWebApp,
            isolationManager = isolationManager,
            context = context,
            analyzer = analyzer,
            faviconFetcher = faviconFetcher,
            simpleIconsManager = simpleIconsManager,
            passwordManager = passwordManager,
            geckoEngineManager = geckoEngineManager,
            themeManager = themeManager,
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.globalNotificationsEnabled)
    }

    @Test
    fun `globalNotificationsEnabled updates when themeManager flow changes`() = runTest {
        val flow = MutableStateFlow(true)
        every { themeManager.globalNotificationsEnabled } returns flow
        viewModel = AppSettingsViewModel(
            appId = 42L,
            getWebAppById = getWebAppById,
            saveWebApp = saveWebApp,
            deleteWebApp = deleteWebApp,
            isolationManager = isolationManager,
            context = context,
            analyzer = analyzer,
            faviconFetcher = faviconFetcher,
            simpleIconsManager = simpleIconsManager,
            passwordManager = passwordManager,
            geckoEngineManager = geckoEngineManager,
            themeManager = themeManager,
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.globalNotificationsEnabled)

        flow.value = false
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.globalNotificationsEnabled)
    }
}
