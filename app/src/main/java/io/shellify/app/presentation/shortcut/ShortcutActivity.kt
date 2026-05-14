package io.shellify.app.presentation.shortcut

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import io.shellify.app.core.shortcut.PwaShortcutManager
import io.shellify.app.presentation.webview.WebViewActivity

/**
 * Transparent trampoline: receives home-screen shortcut taps and
 * immediately forwards to WebViewActivity with the correct app ID.
 * Inspired by AppForge's SplashLauncherActivity.
 */
class ShortcutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appId = intent.getLongExtra(PwaShortcutManager.EXTRA_APP_ID, -1L)
        if (appId != -1L) {
            startActivity(WebViewActivity.launchIntent(this, appId))
        }
        finish()
    }
}
