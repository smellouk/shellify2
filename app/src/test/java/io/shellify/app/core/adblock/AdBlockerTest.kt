package io.shellify.app.core.adblock

import android.net.Uri
import android.webkit.WebResourceRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AdBlockerTest {

    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        adBlocker = AdBlocker()
    }

    private fun makeRequest(url: String, isMainFrame: Boolean = false): WebResourceRequest {
        val uri = mockk<Uri>()
        every { uri.toString() } returns url
        return mockk<WebResourceRequest>().also {
            every { it.isForMainFrame } returns isMainFrame
            every { it.url } returns uri
        }
    }

    @Test
    fun `main frame requests are never blocked`() {
        val request = makeRequest("https://doubleclick.net/ad.js", isMainFrame = true)
        assertNull(adBlocker.shouldBlock(request))
    }

    @Test
    fun `sub-resource to known ad domain is blocked`() {
        val request = makeRequest("https://doubleclick.net/ad.js", isMainFrame = false)
        assertNotNull(adBlocker.shouldBlock(request))
    }

    @Test
    fun `sub-resource to allowed domain is not blocked`() {
        val request = makeRequest("https://example.com/script.js", isMainFrame = false)
        assertNull(adBlocker.shouldBlock(request))
    }

    @Test
    fun `sub-resource matching path pattern is blocked`() {
        val request = makeRequest("https://cdn.example.com/ads/banner.js", isMainFrame = false)
        assertNotNull(adBlocker.shouldBlock(request))
    }

    @Test
    fun `addCustomRules propagates to the cache`() {
        adBlocker.addCustomRules(listOf("||my-custom-ad.com^"))
        val request = makeRequest("https://my-custom-ad.com/tracker.gif", isMainFrame = false)
        assertNotNull(adBlocker.shouldBlock(request))
    }

    @Test
    fun `main frame with ad-like url is still not blocked`() {
        val request = makeRequest("https://ads.example.com/page", isMainFrame = true)
        assertNull(adBlocker.shouldBlock(request))
    }
}
