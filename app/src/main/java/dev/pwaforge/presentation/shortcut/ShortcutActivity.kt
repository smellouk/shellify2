package dev.pwaforge.presentation.shortcut

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import dev.pwaforge.core.shortcut.PwaShortcutManager
import dev.pwaforge.presentation.webview.WebViewActivity

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
            startActivity(
                Intent(this, WebViewActivity::class.java)
                    .putExtra(WebViewActivity.EXTRA_APP_ID, appId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        finish()
    }
}
