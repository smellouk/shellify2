package io.shellify.app.core.webbridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellifyBridgeTest {

    private data class CallRecord(val title: String, val body: String, val icon: String)

    private fun recordingBridge(): Pair<ShellifyBridge, MutableList<CallRecord>> {
        val calls = mutableListOf<CallRecord>()
        val bridge = ShellifyBridge { title, body, icon -> calls.add(CallRecord(title, body, icon)) }
        return bridge to calls
    }

    @Test
    fun `onNotification_withValidInputs_invokesCallback`() {
        val (bridge, calls) = recordingBridge()

        bridge.onNotification("Hi", "Body", "https://x")

        assertEquals(1, calls.size)
        assertEquals(CallRecord("Hi", "Body", "https://x"), calls.first())
    }

    @Test
    fun `onNotification_truncatesOversizedTitle`() {
        val (bridge, calls) = recordingBridge()

        bridge.onNotification("A".repeat(300), "body", "icon")

        assertEquals(1, calls.size)
        assertEquals(256, calls.first().title.length)
    }

    @Test
    fun `onNotification_truncatesOversizedBody`() {
        val (bridge, calls) = recordingBridge()

        bridge.onNotification("ok", "B".repeat(1100), "icon")

        assertEquals(1, calls.size)
        assertEquals(1024, calls.first().body.length)
    }

    @Test
    fun `onNotification_truncatesOversizedIcon`() {
        val (bridge, calls) = recordingBridge()

        bridge.onNotification("ok", "", "I".repeat(3000))

        assertEquals(1, calls.size)
        assertEquals(2048, calls.first().icon.length)
    }

    @Test
    fun `onNotification_withNullTitle_doesNotInvoke`() {
        val (bridge, calls) = recordingBridge()

        bridge.onNotification(null, "body", "icon")

        assertTrue("callback must not be invoked for null title", calls.isEmpty())
    }

    @Test
    fun `onNotification_withBlankTitle_doesNotInvoke`() {
        val (bridge, calls) = recordingBridge()

        bridge.onNotification("   ", "body", "icon")

        assertTrue("callback must not be invoked for blank title", calls.isEmpty())
    }

    @Test
    fun `onNotification_withNullBodyAndIcon_invokesWithEmptyStrings`() {
        val (bridge, calls) = recordingBridge()

        bridge.onNotification("title", null, null)

        assertEquals(1, calls.size)
        assertEquals("", calls.first().body)
        assertEquals("", calls.first().icon)
    }
}
