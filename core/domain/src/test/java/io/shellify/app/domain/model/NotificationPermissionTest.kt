package io.shellify.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NotificationPermissionTest {

    @Test
    fun `NotificationPermission has exactly three values`() {
        val values = NotificationPermission.values()
        assertEquals(3, values.size)
    }

    @Test
    fun `NotificationPermission values are in correct order NOT_ASKED GRANTED DENIED`() {
        val values = NotificationPermission.values()
        assertEquals(NotificationPermission.NOT_ASKED, values[0])
        assertEquals(NotificationPermission.GRANTED, values[1])
        assertEquals(NotificationPermission.DENIED, values[2])
    }

    @Test
    fun `WebApp copy with GRANTED notificationPermission preserves other fields`() {
        val app = WebApp(
            name = "TestApp",
            url = "https://test.com",
            isFullscreen = true,
            adBlockEnabled = false,
        )

        val updated = app.copy(notificationPermission = NotificationPermission.GRANTED)

        assertEquals(NotificationPermission.GRANTED, updated.notificationPermission)
        assertEquals(app.name, updated.name)
        assertEquals(app.url, updated.url)
        assertEquals(app.isFullscreen, updated.isFullscreen)
        assertEquals(app.adBlockEnabled, updated.adBlockEnabled)
    }

    @Test
    fun `WebApp default notificationPermission is NOT_ASKED`() {
        val app = WebApp(name = "App", url = "https://app.com")
        assertEquals(NotificationPermission.NOT_ASKED, app.notificationPermission)
    }

    @Test
    fun `WebApp default dndStartHour is minus 1`() {
        val app = WebApp(name = "App", url = "https://app.com")
        assertEquals(-1, app.dndStartHour)
    }

    @Test
    fun `WebApp default dndEndHour is minus 1`() {
        val app = WebApp(name = "App", url = "https://app.com")
        assertEquals(-1, app.dndEndHour)
    }

    @Test
    fun `WebApp default backgroundNotificationsEnabled is false`() {
        val app = WebApp(name = "App", url = "https://app.com")
        assertEquals(false, app.backgroundNotificationsEnabled)
    }

    @Test
    fun `WebApp copy does not affect unrelated fields when updating notificationPermission`() {
        val original = WebApp(
            name = "MyPwa",
            url = "https://mypwa.com",
            dndStartHour = 22,
            dndEndHour = 8,
            backgroundNotificationsEnabled = true,
        )

        val updated = original.copy(notificationPermission = NotificationPermission.DENIED)

        assertNotEquals(NotificationPermission.NOT_ASKED, updated.notificationPermission)
        assertEquals(NotificationPermission.DENIED, updated.notificationPermission)
        assertEquals(22, updated.dndStartHour)
        assertEquals(8, updated.dndEndHour)
        assertEquals(true, updated.backgroundNotificationsEnabled)
    }
}
