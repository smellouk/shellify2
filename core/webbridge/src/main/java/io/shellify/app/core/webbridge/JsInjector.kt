package io.shellify.app.core.webbridge

/**
 * Contract for engine-side JS injection. WebView/GeckoView engines implement this
 * so the bridge module can request script injection after page load without depending
 * on either engine implementation.
 */
interface JsInjector {
    fun inject(script: String)
}
