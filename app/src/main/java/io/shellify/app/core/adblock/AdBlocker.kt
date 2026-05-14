package io.shellify.app.core.adblock

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

/**
 * Intercepts WebView requests and returns an empty response for blocked URLs.
 * Inspired by AppForge's AdBlocker.
 */
class AdBlocker(private val cache: AdBlockFilterCache = AdBlockFilterCache()) {

    private val emptyResponse = WebResourceResponse("text/plain", "utf-8", "".byteInputStream())

    fun shouldBlock(request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        // Never block the main frame — only sub-resources (scripts, images, iframes, etc.)
        if (request.isForMainFrame) return null
        return if (cache.shouldBlock(url)) emptyResponse else null
    }

    fun addCustomRules(rules: List<String>) = cache.addCustomRules(rules)
}
