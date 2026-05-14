package io.shellify.app.core.adblock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdBlockFilterCacheTest {

    private lateinit var cache: AdBlockFilterCache

    @Before
    fun setUp() {
        cache = AdBlockFilterCache()
    }

    // ── built-in host blocking ────────────────────────────────────────────────

    @Test
    fun `blocks exact known ad domain`() {
        assertTrue(cache.shouldBlock("https://doubleclick.net/ad"))
    }

    @Test
    fun `blocks domain with www prefix stripped`() {
        assertTrue(cache.shouldBlock("https://www.doubleclick.net/ad"))
    }

    @Test
    fun `blocks googlesyndication domain`() {
        assertTrue(cache.shouldBlock("https://googlesyndication.com/pagead/js/adsbygoogle.js"))
    }

    @Test
    fun `blocks criteo domain`() {
        assertTrue(cache.shouldBlock("https://criteo.com/tracking?id=123"))
    }

    @Test
    fun `blocks taboola domain`() {
        assertTrue(cache.shouldBlock("https://taboola.com/widget"))
    }

    @Test
    fun `blocks hotjar domain`() {
        assertTrue(cache.shouldBlock("https://hotjar.com/script.js"))
    }

    @Test
    fun `does not block legitimate domain`() {
        assertFalse(cache.shouldBlock("https://example.com/page"))
    }

    @Test
    fun `does not block google search`() {
        assertFalse(cache.shouldBlock("https://google.com/search?q=test"))
    }

    @Test
    fun `does not block github`() {
        assertFalse(cache.shouldBlock("https://github.com/user/repo"))
    }

    // ── built-in pattern blocking ─────────────────────────────────────────────

    @Test
    fun `blocks url containing ads path pattern`() {
        assertTrue(cache.shouldBlock("https://example.com/ads/banner.png"))
    }

    @Test
    fun `blocks url containing ad path pattern`() {
        assertTrue(cache.shouldBlock("https://cdn.example.com/ad/spot.js"))
    }

    @Test
    fun `blocks url containing adsbygoogle pattern`() {
        assertTrue(cache.shouldBlock("https://example.com/static/adsbygoogle.js"))
    }

    @Test
    fun `blocks url containing pagead pattern`() {
        assertTrue(cache.shouldBlock("https://example.com/pagead/js/script.js"))
    }

    @Test
    fun `blocks url containing vast pattern`() {
        assertTrue(cache.shouldBlock("https://video.example.com/vast/ad.xml"))
    }

    @Test
    fun `pattern matching is case insensitive`() {
        assertTrue(cache.shouldBlock("https://example.com/ADS/Banner.png"))
    }

    // ── URL without scheme ────────────────────────────────────────────────────

    @Test
    fun `returns false for malformed url without scheme`() {
        assertFalse(cache.shouldBlock("doubleclick.net/ad"))
    }

    @Test
    fun `returns false for empty url`() {
        assertFalse(cache.shouldBlock(""))
    }

    // ── custom rules ─────────────────────────────────────────────────────────

    @Test
    fun `addCustomRules blocks domain-style rule`() {
        cache.addCustomRules(listOf("||custom-ads.com^"))
        assertTrue(cache.shouldBlock("https://custom-ads.com/script.js"))
    }

    @Test
    fun `addCustomRules blocks path-style rule`() {
        cache.addCustomRules(listOf("/tracking/pixel"))
        assertTrue(cache.shouldBlock("https://example.com/tracking/pixel?id=1"))
    }

    @Test
    fun `addCustomRules strips adblock filter syntax`() {
        cache.addCustomRules(listOf("||ads.tracker.io^"))
        assertTrue(cache.shouldBlock("https://ads.tracker.io/pixel"))
    }

    @Test
    fun `addCustomRules strips www prefix from domain rules`() {
        cache.addCustomRules(listOf("||www.spamads.net^"))
        assertTrue(cache.shouldBlock("https://www.spamads.net/ad"))
        assertTrue(cache.shouldBlock("https://spamads.net/ad"))
    }

    @Test
    fun `addCustomRules with multiple rules all apply`() {
        cache.addCustomRules(listOf("||site-a.com^", "||site-b.com^"))
        assertTrue(cache.shouldBlock("https://site-a.com/img.png"))
        assertTrue(cache.shouldBlock("https://site-b.com/img.png"))
    }

    @Test
    fun `addCustomRules does not affect unrelated urls`() {
        cache.addCustomRules(listOf("||only-this.com^"))
        assertFalse(cache.shouldBlock("https://other-site.com/page"))
    }
}
