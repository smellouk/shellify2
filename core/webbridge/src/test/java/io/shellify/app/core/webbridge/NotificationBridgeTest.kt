package io.shellify.app.core.webbridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationBridgeTest {

    @Test
    fun `buildScript_isNonEmpty_andContainsKeyHooks`() {
        val script = NotificationBridge.buildScript()
        assertTrue("Script must not be empty", script.isNotEmpty())
        assertTrue(
            "Script must call window.ShellifyBridge.onNotification",
            script.contains("window.ShellifyBridge.onNotification"),
        )
        assertTrue(
            "Script must reference window.Notification",
            script.contains("window.Notification"),
        )
        assertTrue(
            "Script must reference requestPermission",
            script.contains("requestPermission"),
        )
        assertTrue(
            "Script must contain idempotency guard __shellifyNotificationLoaded",
            script.contains("__shellifyNotificationLoaded"),
        )
    }

    @Test
    fun `buildScript_isStable_acrossCalls`() {
        val first = NotificationBridge.buildScript()
        val second = NotificationBridge.buildScript()
        assertEquals("buildScript() must return identical content on repeated calls", first, second)
    }
}
