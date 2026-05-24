package io.shellify.app.core.engine

import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for GeckoViewEngine.canScrollUp().
 *
 * GeckoSession and GeckoRuntime require native libraries unavailable in the JVM unit test
 * environment. The GeckoViewEngine constructor only stores its arguments — it does not
 * touch GeckoSession or GeckoRuntime until createView() is called.
 *
 * We therefore:
 *  1. Instantiate GeckoViewEngine with mock collaborators (no native call triggered).
 *  2. Drive private geckoScrollY via reflection, mirroring what ScrollDelegate and
 *     NavigationDelegate do at runtime.
 *  3. Assert canScrollUp() returns the expected value.
 */
class GeckoViewEngineScrollTest {

    private fun buildEngine(): GeckoViewEngine {
        val context = mockk<android.content.Context>(relaxed = true)
        val engineManager = mockk<GeckoEngineManager>(relaxed = true)
        return GeckoViewEngine(context, engineManager)
    }

    /** Mimics GeckoSession.ScrollDelegate.onScrollChanged setting geckoScrollY. */
    private fun setScrollY(engine: GeckoViewEngine, value: Int) {
        val field = GeckoViewEngine::class.java.getDeclaredField("geckoScrollY")
        field.isAccessible = true
        field.setInt(engine, value)
    }

    @Test
    fun canScrollUp_returnsFalse_whenGeckoScrollYIsZero() {
        // Arrange: initial state — no scroll has occurred yet
        val engine = buildEngine()
        // geckoScrollY is 0 by default; no mutation needed

        // Act + Assert
        assertFalse(
            "canScrollUp() must return false when geckoScrollY == 0 (initial state)",
            engine.canScrollUp(),
        )
    }

    @Test
    fun canScrollUp_returnsTrue_whenGeckoScrollYIsPositive() {
        // Arrange: simulate ScrollDelegate.onScrollChanged(session, 0, 42)
        val engine = buildEngine()
        setScrollY(engine, 42)

        // Act + Assert
        assertTrue(
            "canScrollUp() must return true when geckoScrollY > 0",
            engine.canScrollUp(),
        )
    }

    @Test
    fun canScrollUp_returnsFalse_afterNavigationResetsScrollY() {
        // Arrange: scroll happened, then navigation fires onLocationChange which resets geckoScrollY=0
        val engine = buildEngine()
        setScrollY(engine, 100) // simulate prior scroll
        assertTrue("pre-condition: canScrollUp() is true before reset", engine.canScrollUp())

        // Simulate NavigationDelegate.onLocationChange → geckoScrollY = 0
        setScrollY(engine, 0)

        // Act + Assert
        assertFalse(
            "canScrollUp() must return false after navigation resets geckoScrollY to 0",
            engine.canScrollUp(),
        )
    }
}
