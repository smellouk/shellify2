package io.shellify.app.presentation.webview

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.shellify.app.core.isolation.IsolationManager
import io.shellify.app.core.security.PasswordManager
import io.shellify.app.domain.model.NotificationPermission
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.usecase.SaveWebAppUseCase
import io.shellify.app.presentation.webview.PwaNotificationDispatcher.DispatchResult
import io.shellify.app.presentation.webview.WebViewViewModel.PermissionDialogState
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
class WebViewViewModelNotificationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val isolationManager = mockk<IsolationManager>(relaxed = true)
    private val saveWebApp = mockk<SaveWebAppUseCase>(relaxed = true)
    private val passwordManager = mockk<PasswordManager>(relaxed = true)
    private val dispatcher = mockk<PwaNotificationDispatcher>(relaxed = true)

    private val grantedApp = WebApp(
        id = 1L, name = "TestApp", url = "https://test.com",
        notificationPermission = NotificationPermission.GRANTED,
    )
    private val deniedApp = grantedApp.copy(notificationPermission = NotificationPermission.DENIED)
    private val notAskedApp = grantedApp.copy(notificationPermission = NotificationPermission.NOT_ASKED)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { passwordManager.passwordHash } returns MutableStateFlow(null)
        every { passwordManager.wipeOnFailedAttempts } returns MutableStateFlow(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vmWith(app: WebApp) = WebViewViewModel(
        initialApp = app,
        isolationManager = isolationManager,
        saveWebApp = saveWebApp,
        passwordManager = passwordManager,
        notificationDispatcher = dispatcher,
    )

    @Test
    fun `onNotificationPermissionRequested when granted invokes callback true immediately`() = runTest {
        val vm = vmWith(grantedApp)
        var callbackResult: Boolean? = null

        vm.onNotificationPermissionRequested { granted -> callbackResult = granted }

        assertEquals(true, callbackResult)
        assertTrue(vm.permissionDialog.value is PermissionDialogState.Hidden)
    }

    @Test
    fun `onNotificationPermissionRequested when denied invokes callback false immediately`() = runTest {
        val vm = vmWith(deniedApp)
        var callbackResult: Boolean? = null

        vm.onNotificationPermissionRequested { granted -> callbackResult = granted }

        assertEquals(false, callbackResult)
        assertTrue(vm.permissionDialog.value is PermissionDialogState.Hidden)
    }

    @Test
    fun `onNotificationPermissionRequested when not asked shows dialog`() = runTest {
        val vm = vmWith(notAskedApp)
        var callbackInvoked = false

        vm.onNotificationPermissionRequested { callbackInvoked = true }

        assertFalse(callbackInvoked)
        val state = vm.permissionDialog.value
        assertTrue(state is PermissionDialogState.Shown)
        assertEquals("TestApp", (state as PermissionDialogState.Shown).appName)
    }

    @Test
    fun `onPermissionDialogResult true persists granted and invokes pending callback`() = runTest {
        val vm = vmWith(notAskedApp)
        var callbackResult: Boolean? = null
        vm.onNotificationPermissionRequested { granted -> callbackResult = granted }

        vm.onPermissionDialogResult(true)
        advanceUntilIdle()

        assertEquals(true, callbackResult)
        coVerify { saveWebApp(match { it.notificationPermission == NotificationPermission.GRANTED }) }
        assertTrue(vm.permissionDialog.value is PermissionDialogState.Hidden)
    }

    @Test
    fun `onPermissionDialogResult false persists denied and invokes pending callback`() = runTest {
        val vm = vmWith(notAskedApp)
        var callbackResult: Boolean? = null
        vm.onNotificationPermissionRequested { granted -> callbackResult = granted }

        vm.onPermissionDialogResult(false)
        advanceUntilIdle()

        assertEquals(false, callbackResult)
        coVerify { saveWebApp(match { it.notificationPermission == NotificationPermission.DENIED }) }
        assertTrue(vm.permissionDialog.value is PermissionDialogState.Hidden)
    }

    @Test
    fun `onNotificationReceived calls dispatcher`() = runTest {
        val vm = vmWith(grantedApp)
        coEvery { dispatcher.dispatch(any(), any(), any(), any(), any()) } returns DispatchResult.Posted(42)

        vm.onNotificationReceived("Title", "Body", null, null)
        advanceUntilIdle()

        coVerify { dispatcher.dispatch(grantedApp, "Title", "Body", null, null) }
    }
}
