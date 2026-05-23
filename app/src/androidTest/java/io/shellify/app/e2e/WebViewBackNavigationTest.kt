package io.shellify.app.e2e

import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.shellify.app.presentation.webview.WebViewActivity
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * E2E back-navigation test: opens shellify.app home, navigates to terms.html,
 * presses back, and verifies the activity stays alive (history navigation, not app exit).
 *
 * Requires internet access on the test device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class WebViewBackNavigationTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val pageLoad = PageLoadIdlingResource()

    private var scenario: ActivityScenario<WebViewActivity>? = null

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(pageLoad)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(pageLoad)
        runCatching { scenario?.onActivity { it.pageFinishedCallback = null } }
    }

    @Test
    fun backFromTerms_navigatesInHistory_doesNotExitApp() {
        scenario = ActivityScenario.launch<WebViewActivity>(homeIntent())
        scenario!!.onActivity { it.pageFinishedCallback = { pageLoad.onPageFinished() } }

        // Idle until shellify.app home page finishes loading
        pageLoad.awaitIdle()
        pageLoad.reset()

        val s = scenario!!

        // Navigate to terms.html — creates a back-stack entry in the WebView
        s.onActivity { it.navigateTo("https://shellify.app/terms.html") }

        // Idle until terms page finishes loading
        pageLoad.awaitIdle()

        // Press back — must navigate within browser history, NOT exit the activity
        s.onActivity { it.onBackPressedDispatcher.onBackPressed() }

        // Activity must still be alive — back went to the previous page
        assertNotEquals(Lifecycle.State.DESTROYED, s.state)

        // Tear down: wait for onDestroy so the CountDownLatch — not waitForIdleSync() —
        // gates the cleanup. This prevents Compose frame callbacks from causing a hang.
        val destroyed = CountDownLatch(1)
        s.onActivity { activity ->
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) = destroyed.countDown()
            })
            activity.finish()
        }
        destroyed.await(10, TimeUnit.SECONDS)
    }

    private fun homeIntent() = Intent(context, WebViewActivity::class.java)
        .putExtra(WebViewActivity.EXTRA_PREVIEW_URL, "https://shellify.app")
        .putExtra(WebViewActivity.EXTRA_PREVIEW_NAME, "Shellify")
}

/**
 * IdlingResource that becomes idle when [onPageFinished] is called.
 * Call [reset] before triggering a new navigation so the next [awaitIdle] blocks until
 * that page's load completes.
 */
class PageLoadIdlingResource : IdlingResource {

    @Volatile private var idle = false
    @Volatile private var latch = CountDownLatch(1)
    private var registeredCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = "page-load"
    override fun isIdleNow(): Boolean = idle

    override fun registerIdleTransitionCallback(cb: IdlingResource.ResourceCallback) {
        registeredCallback = cb
        if (idle) cb.onTransitionToIdle()
    }

    fun onPageFinished() {
        idle = true
        latch.countDown()
        registeredCallback?.onTransitionToIdle()
    }

    /** Reset to busy before starting the next navigation. */
    fun reset() {
        idle = false
        latch = CountDownLatch(1)
    }

    /** Blocks the calling (test) thread until the page finishes or timeout elapses. */
    fun awaitIdle(timeoutSeconds: Long = 15) {
        if (!idle) latch.await(timeoutSeconds, TimeUnit.SECONDS)
    }
}
