package dev.pwaforge.core.isolation

import android.content.Context
import android.os.Build
import android.webkit.WebView
import dev.pwaforge.core.crypto.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Single entry-point for all PWA isolation logic.
 *
 * - API 33+  → WebView Profiles (complete isolation: cookies, localStorage, IndexedDB, cache)
 * - API 23–32 → CookieJarManager (encrypted cookie save/restore) +
 *               same-origin policy already isolates localStorage per domain
 */
class IsolationManager(context: Context, crypto: CryptoManager) {

    private val cookieJarManager = CookieJarManager(context, crypto)
    private val scope = CoroutineScope(Dispatchers.Main)

    fun applyTo(webView: WebView, isolationId: String, url: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WebViewProfileManager.applyProfile(webView, isolationId)
        } else {
            scope.launch { cookieJarManager.restoreFor(isolationId) }
        }
    }

    fun onSessionEnd(isolationId: String, currentUrl: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            scope.launch { cookieJarManager.saveAndClearFor(isolationId, currentUrl) }
        }
    }

    fun clearData(isolationId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WebViewProfileManager.deleteProfile(isolationId)
        } else {
            scope.launch { cookieJarManager.deleteFor(isolationId) }
        }
    }
}
