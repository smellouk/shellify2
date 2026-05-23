package io.shellify.app.core.engine

import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class GeckoViewEngineNotificationTest {

    @Test
    fun `dispatchNotification with title invokes callback`() {
        val cb = mockk<BrowserEngineCallback>(relaxed = true)
        val payload = NotificationPayload(title = "Hi", body = "Body", iconUrl = "icon", tag = "t1")

        dispatchNotification(payload, cb)

        verify(exactly = 1) { cb.onNotificationReceived("Hi", "Body", "icon", "t1") }
    }

    @Test
    fun `dispatchNotification with null title does not invoke callback`() {
        val cb = mockk<BrowserEngineCallback>(relaxed = true)
        val payload = NotificationPayload(title = null, body = "Body", iconUrl = "icon", tag = "t1")

        dispatchNotification(payload, cb)

        verify(exactly = 0) { cb.onNotificationReceived(any(), any(), any(), any()) }
    }

    @Test
    fun `dispatchNotification with null body and icon passes nulls`() {
        val cb = mockk<BrowserEngineCallback>(relaxed = true)
        // tag is @NonNull in GeckoView 140 WebNotification; empty string signals absent tag.
        val payload = NotificationPayload(title = "OK", body = null, iconUrl = null, tag = "")

        dispatchNotification(payload, cb)

        verify(exactly = 1) { cb.onNotificationReceived("OK", null, null, "") }
    }
}
