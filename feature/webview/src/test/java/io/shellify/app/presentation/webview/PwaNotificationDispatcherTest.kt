package io.shellify.app.presentation.webview

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.shellify.app.domain.model.NotificationPermission
import io.shellify.app.domain.model.PwaNotification
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.usecase.CountNotificationsTodayUseCase
import io.shellify.app.domain.usecase.IsDndActiveUseCase
import io.shellify.app.domain.usecase.SaveNotificationUseCase
import io.shellify.app.presentation.webview.PwaNotificationDispatcher.DispatchResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PwaNotificationDispatcherTest {

    private lateinit var context: Context
    private val isDndActive = mockk<IsDndActiveUseCase>(relaxed = true)
    private val saveNotification = mockk<SaveNotificationUseCase>()
    private val countToday = mockk<CountNotificationsTodayUseCase>()
    private val mockManager = mockk<NotificationManagerCompat>(relaxed = true)

    private fun buildDispatcher(
        checkPostPermission: (Context) -> Boolean = { true },
        isGlobalNotificationsEnabled: () -> Boolean = { true },
    ) = PwaNotificationDispatcher(
        context = context,
        isGlobalNotificationsEnabled = isGlobalNotificationsEnabled,
        isDndActive = isDndActive,
        saveNotification = saveNotification,
        countToday = countToday,
        notificationManagerProvider = { mockManager },
        checkPostPermission = checkPostPermission,
        channelNameProvider = { name -> "$name notifications" },
        channelDescProvider = { name -> "Notifications from $name" },
    )

    private fun appWith(
        permission: NotificationPermission,
        isolationId: String = "test-iso",
        dndStart: Int = -1,
        dndEnd: Int = -1,
    ) = WebApp(
        id = 1L,
        name = "Test App",
        url = "https://example.com",
        isolationId = isolationId,
        notificationPermission = permission,
        dndStartHour = dndStart,
        dndEndHour = dndEnd,
    )

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `dispatch with denied permission returns Dropped and does not post`() = runTest {
        val app = appWith(NotificationPermission.DENIED)
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Dropped.PermissionDenied)
        coVerify(exactly = 0) { mockManager.notify(any(), any()) }
        coVerify(exactly = 0) { saveNotification(any()) }
    }

    @Test
    fun `dispatch with not asked permission drops as NotAsked`() = runTest {
        val app = appWith(NotificationPermission.NOT_ASKED)
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Dropped.NotAsked)
        coVerify(exactly = 0) { mockManager.notify(any(), any()) }
        coVerify(exactly = 0) { saveNotification(any()) }
    }

    @Test
    fun `dispatch with DND active drops as DndActive`() = runTest {
        val app = appWith(NotificationPermission.GRANTED, dndStart = 22, dndEnd = 8)
        every { isDndActive(22, 8, any()) } returns true
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Dropped.DndActive)
        coVerify(exactly = 0) { mockManager.notify(any(), any()) }
        coVerify(exactly = 0) { saveNotification(any()) }
    }

    @Test
    fun `dispatch over rate limit drops as RateLimited`() = runTest {
        val app = appWith(NotificationPermission.GRANTED)
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 100
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Dropped.RateLimited)
        coVerify(exactly = 0) { mockManager.notify(any(), any()) }
        coVerify(exactly = 0) { saveNotification(any()) }
    }

    @Test
    fun `dispatch at 101 is also rate limited`() = runTest {
        val app = appWith(NotificationPermission.GRANTED)
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 101
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Dropped.RateLimited)
    }

    @Test
    fun `dispatch at 99 proceeds to post`() = runTest {
        val app = appWith(NotificationPermission.GRANTED)
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 99
        coEvery { saveNotification(any()) } returns 1L
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Posted)
        coVerify(exactly = 1) { mockManager.notify(any(), any()) }
    }

    @Test
    fun `dispatch with granted and under limit posts and saves`() = runTest {
        val app = appWith(NotificationPermission.GRANTED, isolationId = "abc123")
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 0
        val notificationSlot = slot<PwaNotification>()
        coEvery { saveNotification(capture(notificationSlot)) } returns 1L
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Hello", "World", null, null)

        assertTrue(result is DispatchResult.Posted)
        coVerify(exactly = 1) { mockManager.notify(any(), any()) }
        coVerify(exactly = 1) { saveNotification(any()) }
        assertNotNull(notificationSlot.captured)
        assertEquals("Hello", notificationSlot.captured.title)
        assertEquals("World", notificationSlot.captured.body)
    }

    @Test
    fun `dispatch title truncated to 256`() = runTest {
        val app = appWith(NotificationPermission.GRANTED)
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 0
        val notificationSlot = slot<PwaNotification>()
        coEvery { saveNotification(capture(notificationSlot)) } returns 1L
        val longTitle = "A".repeat(300)
        val dispatcher = buildDispatcher()

        dispatcher.dispatch(app, longTitle, "Body", null, null)

        assertEquals(256, notificationSlot.captured.title.length)
    }

    @Test
    fun `dispatch body truncated to 1024`() = runTest {
        val app = appWith(NotificationPermission.GRANTED)
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 0
        val notificationSlot = slot<PwaNotification>()
        coEvery { saveNotification(capture(notificationSlot)) } returns 1L
        val longBody = "B".repeat(1200)
        val dispatcher = buildDispatcher()

        dispatcher.dispatch(app, "Title", longBody, null, null)

        assertEquals(1024, notificationSlot.captured.body?.length)
    }

    @Test
    fun `dispatch channel id uses isolation id`() = runTest {
        val app = appWith(NotificationPermission.GRANTED, isolationId = "abc123")
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 0
        coEvery { saveNotification(any()) } returns 1L
        val dispatcher = buildDispatcher()

        val result = dispatcher.dispatch(app, "Title", null, null, null)

        assertTrue(result is DispatchResult.Posted)
        coVerify(exactly = 1) { mockManager.notify(any(), any()) }
    }

    @Test
    fun `dispatch when globally disabled drops as GloballyDisabled before any other gate`() = runTest {
        val app = appWith(NotificationPermission.GRANTED)
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 0
        val dispatcher = buildDispatcher(isGlobalNotificationsEnabled = { false })

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Dropped.GloballyDisabled)
        coVerify(exactly = 0) { mockManager.notify(any(), any()) }
        coVerify(exactly = 0) { saveNotification(any()) }
    }

    @Test
    fun `dispatch with OS permission missing drops without posting`() = runTest {
        val app = appWith(NotificationPermission.GRANTED)
        every { isDndActive(any(), any(), any()) } returns false
        coEvery { countToday(app.id, any()) } returns 0
        val dispatcher = buildDispatcher(checkPostPermission = { false })

        val result = dispatcher.dispatch(app, "Title", "Body", null, null)

        assertTrue(result is DispatchResult.Dropped.OsPermissionMissing)
        coVerify(exactly = 0) { mockManager.notify(any(), any()) }
        coVerify(exactly = 0) { saveNotification(any()) }
    }
}
