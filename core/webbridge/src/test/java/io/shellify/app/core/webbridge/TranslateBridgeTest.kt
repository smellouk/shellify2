package io.shellify.app.core.webbridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslateBridgeTest {

    @Test
    fun `buildScript returns non-empty string with target language and idempotency guard`() {
        val script = TranslateBridge.buildScript("en", true)
        assertTrue("Script must not be empty", script.isNotEmpty())
        assertTrue(
            "Script must contain the target language",
            script.contains("en"),
        )
        assertTrue(
            "Script must contain the idempotency guard __shellifyTranslateLoaded",
            script.contains("__shellifyTranslateLoaded"),
        )
    }

    @Test
    fun `buildScript returns identical strings for the same arguments`() {
        val first = TranslateBridge.buildScript("fr", false)
        val second = TranslateBridge.buildScript("fr", false)
        assertEquals("buildScript() must be stable — same args must produce same output", first, second)
    }
}
