package io.shellify.app.presentation.webview

import android.content.Intent
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackgroundNotificationServiceTest {

    @Test
    fun `onStartCommand calls startForeground first`() {
        val controller = Robolectric.buildService(BackgroundNotificationService::class.java).create()
        val service = controller.get()

        val intent = Intent(service, BackgroundNotificationService::class.java).apply {
            putExtra(BackgroundNotificationService.EXTRA_APP_ID, 42L)
        }
        service.onStartCommand(intent, 0, 1)

        val shadow = Shadows.shadowOf(service)
        assertNotNull("startForeground must be called", shadow.lastForegroundNotification)
        assertTrue(
            "Notification ID must equal SERVICE_NOTIFICATION_ID",
            shadow.lastForegroundNotificationId != 0,
        )
    }

    @Test
    fun `onStartCommand with missing EXTRA_APP_ID calls stopSelf`() {
        val controller = Robolectric.buildService(BackgroundNotificationService::class.java).create()
        val service = controller.get()

        // Intent without EXTRA_APP_ID (defaults to -1)
        val intent = Intent(service, BackgroundNotificationService::class.java)
        service.onStartCommand(intent, 0, 1)

        val shadow = Shadows.shadowOf(service)
        assertTrue("Service must have called stopSelf on missing appId", shadow.isStoppedBySelf)
    }

    @Test
    fun `onStartCommand with ACTION_STOP calls stopSelf`() {
        val controller = Robolectric.buildService(BackgroundNotificationService::class.java).create()
        val service = controller.get()

        val intent = Intent(service, BackgroundNotificationService::class.java).apply {
            action = BackgroundNotificationService.ACTION_STOP
        }
        service.onStartCommand(intent, 0, 1)

        val shadow = Shadows.shadowOf(service)
        assertTrue("Service must have called stopSelf on ACTION_STOP", shadow.isStoppedBySelf)
    }
}
