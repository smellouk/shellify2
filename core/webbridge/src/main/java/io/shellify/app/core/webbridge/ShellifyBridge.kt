package io.shellify.app.core.webbridge

import android.util.Log
import android.webkit.JavascriptInterface

/**
 * Kotlin entry point for the System WebView JS notification bridge.
 * Added to WebView via addJavascriptInterface(ShellifyBridge(...), "ShellifyBridge").
 *
 * All inputs from @JavascriptInterface are fully untrusted (T-06-06): the web page can
 * call onNotification() with arbitrary content. Inputs are validated and truncated
 * before being forwarded to the callback. A thrown callback exception is swallowed via
 * runCatching so a misbehaving caller cannot crash the WebView thread.
 */
class ShellifyBridge(
    private val permissionProvider: () -> String = { "UNKNOWN" },
    private val notificationCallback: (title: String, body: String, icon: String) -> Unit,
) {

    companion object {
        private const val TAG = "ShellifyBridge"
        const val MAX_TITLE_LEN = 256
        const val MAX_BODY_LEN = 1024
        const val MAX_ICON_LEN = 2048
    }

    @JavascriptInterface
    fun getNotificationPermission(): String = permissionProvider()

    @JavascriptInterface
    fun onNotification(title: String?, body: String?, icon: String?) {
        if (title.isNullOrBlank()) {
            Log.d(TAG, "onNotification: rejected — null or blank title")
            return
        }

        val safeTitle = title.take(MAX_TITLE_LEN)
        val safeBody = (body ?: "").take(MAX_BODY_LEN)
        val safeIcon = (icon ?: "").take(MAX_ICON_LEN)

        Log.d(TAG, "onNotification: title='${safeTitle.take(40)}'")

        runCatching { notificationCallback(safeTitle, safeBody, safeIcon) }
            .onFailure { Log.d(TAG, "onNotification: callback threw", it) }
    }
}
