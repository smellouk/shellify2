package dev.pwaforge.core.adblock

/**
 * Compiled ad-block rules split into exact-host sets and substring patterns.
 * Inspired by AppForge's AdBlockFilterCache.
 */
class AdBlockFilterCache {

    private val blockedHosts = mutableSetOf<String>()
    private val blockedPatterns = mutableListOf<String>()

    init {
        loadBuiltInRules()
    }

    fun shouldBlock(url: String): Boolean {
        val host = extractHost(url) ?: return false
        if (host in blockedHosts) return true
        return blockedPatterns.any { url.contains(it, ignoreCase = true) }
    }

    private fun extractHost(url: String): String? = runCatching {
        val start = url.indexOf("://").takeIf { it >= 0 }?.plus(3) ?: return@runCatching null
        val end = url.indexOf('/', start).takeIf { it > 0 } ?: url.length
        url.substring(start, end).lowercase().removePrefix("www.")
    }.getOrNull()

    private fun loadBuiltInRules() {
        // Common ad/tracker domains
        blockedHosts += setOf(
            "doubleclick.net",
            "googleadservices.com",
            "googlesyndication.com",
            "adnxs.com",
            "advertising.com",
            "ads.yahoo.com",
            "pagead2.googlesyndication.com",
            "adservice.google.com",
            "amazon-adsystem.com",
            "adsymptotic.com",
            "moatads.com",
            "criteo.com",
            "criteo.net",
            "outbrain.com",
            "taboola.com",
            "revcontent.com",
            "pubmatic.com",
            "openx.net",
            "rubiconproject.com",
            "appnexus.com",
            "smartadserver.com",
            "adform.net",
            "quantserve.com",
            "scorecardresearch.com",
            "omtrdc.net",
            "adobedtm.com",
            "demdex.net",
            "adsafeprotected.com",
            "googletagservices.com",
            "googletagmanager.com",
            "yandex.ru",
            "mc.yandex.ru",
            "hotjar.com",
            "mixpanel.com",
            "segment.com",
            "segment.io",
            "fullstory.com",
            "mouseflow.com",
        )

        // Substring patterns for ad paths
        blockedPatterns += listOf(
            "/ads/",
            "/ad/",
            "/adserver/",
            "/banners/",
            "/pop.js",
            "/popup.js",
            "pagead",
            "adsbygoogle",
            "prebid.js",
            "/vast/",
            "/vpaid/",
        )
    }

    fun addCustomRules(rules: List<String>) {
        rules.forEach { rule ->
            val cleaned = rule.trim().removePrefix("||").removeSuffix("^")
            if (cleaned.contains('/')) blockedPatterns += cleaned
            else blockedHosts += cleaned.lowercase().removePrefix("www.")
        }
    }
}
