package io.shellify.app.presentation.linkdispatcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.shellify.app.core.deeplink.DeepLinkHandler
import io.shellify.app.presentation.theme.ShellifyTheme
import kotlinx.coroutines.launch

class LinkDispatcherActivity : ComponentActivity() {

    private lateinit var viewModel: LinkDispatcherViewModel
    private lateinit var services: LinkDispatcherServiceProvider
    private var currentUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        services = application as LinkDispatcherServiceProvider
        viewModel = ViewModelProvider(
            this,
            LinkDispatcherViewModel.Factory(services.findAppsForUrl),
        )[LinkDispatcherViewModel::class.java]

        val url = resolveUrl(intent)
        if (url == null) {
            finish()
            return
        }

        currentUrl = url
        viewModel.dispatch(url)

        lifecycleScope.launch {
            viewModel.commands.collect { handleCommand(it) }
        }

        setContent {
            ShellifyTheme {
                val uiState by viewModel.uiState.collectAsState()
                LinkDispatcherSheet(
                    uiState = uiState,
                    onAppSelected = { selectedApp, selectedUrl ->
                        viewModel.onAppSelected(selectedApp, selectedUrl)
                    },
                    onDismiss = { viewModel.onDismiss() },
                )
            }
        }
    }

    private fun resolveUrl(intent: Intent): String? {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: return null
                extractUrlFromShareText(text)
            }
            Intent.ACTION_VIEW -> {
                val data = intent.data ?: return null
                DeepLinkHandler.parseOpen(data)
            }
            else -> null
        }
    }

    private fun handleCommand(command: LinkDispatcherCommand) {
        when (command) {
            is LinkDispatcherCommand.LaunchApp -> {
                startActivity(
                    services.webViewIntentFactory.previewIntent(this, command.url, command.app.name, command.app.lockType),
                )
                finish()
            }
            is LinkDispatcherCommand.AddAsNew -> {
                val addIntent = Intent(Intent.ACTION_VIEW, Uri.parse(DeepLinkHandler.buildCustomScheme(command.url, "")))
                    .setPackage(packageName)
                startActivity(addIntent)
                finish()
            }
            is LinkDispatcherCommand.FallbackToBrowser -> finish()
        }
    }
}

// Apps like Chrome share as "Page Title\nhttps://…" — extract the first http(s) URL wherever it appears.
internal fun extractUrlFromShareText(text: String): String? =
    Regex("https?://\\S+").find(text)?.value
