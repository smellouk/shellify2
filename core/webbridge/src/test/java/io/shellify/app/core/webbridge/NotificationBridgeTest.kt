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

    @Test
    fun `buildPermissionRequestScript_isNonEmpty_andContainsKeyHooks`() {
        val script = NotificationBridge.buildPermissionRequestScript()
        assertTrue("Script must not be empty", script.isNotEmpty())
        assertTrue(
            "Script must call ShellifyBridge.requestNotificationPermission",
            script.contains("ShellifyBridge.requestNotificationPermission"),
        )
        assertTrue(
            "Script must expose __shellifyResolvePermission resolver",
            script.contains("__shellifyResolvePermission"),
        )
        assertTrue(
            "Script must intercept requestPermission",
            script.contains("requestPermission"),
        )
        assertTrue(
            "Script must contain idempotency guard __shellifyPermRequestLoaded",
            script.contains("__shellifyPermRequestLoaded"),
        )
    }

    @Test
    fun `buildPermissionRequestScript_isStable_acrossCalls`() {
        val first = NotificationBridge.buildPermissionRequestScript()
        val second = NotificationBridge.buildPermissionRequestScript()
        assertEquals("buildPermissionRequestScript() must be stable across calls", first, second)
    }
}
