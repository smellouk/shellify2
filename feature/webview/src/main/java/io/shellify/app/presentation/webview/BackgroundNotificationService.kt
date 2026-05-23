package io.shellify.app.presentation.webview

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.shellify.app.core.engine.BrowserEngineCallback
import io.shellify.app.core.engine.NotificationDelegateFactory
import io.shellify.app.domain.model.NotificationPermission
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.usecase.IsDndActiveUseCase
import io.shellify.core.ui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.WebNotification
import org.mozilla.geckoview.WebNotificationDelegate

class BackgroundNotificationService : Service() {

    companion object {
        const val EXTRA_APP_ID = "extra_app_id"
        const val ACTION_STOP = "io.shellify.app.action.STOP_BG_NOTIFICATIONS"
        @Suppress("MagicNumber")
        private const val SERVICE_NOTIFICATION_ID = 0x06_5E_57_1C
        const val SERVICE_CHANNEL_ID = "shellify_bg_service"
        private const val TAG = "BgNotificationService"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // MUST be the very first statement (T-06-28): Android will ANR if startForeground
        // is not called promptly when the service is started via startForegroundService.
        startForeground(SERVICE_NOTIFICATION_ID, buildServiceNotification(null))

        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val appId = intent?.getLongExtra(EXTRA_APP_ID, -1L) ?: -1L
        if (appId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        scope.launch { initSession(appId) }
        return START_STICKY
    }

    private suspend fun initSession(appId: Long) {
        val provider = application as WebViewServiceProvider

        // Ensure GeckoView native libs are loaded before accessing the runtime.
        provider.injectAndLoadGeckoView()

        val webApp: WebApp = provider.getWebAppById(appId) ?: run {
            Log.w(TAG, "WebApp $appId not found; stopping service")
            stopSelf()
            return
        }

        // Update the ongoing notification to show the app name.
        @Suppress("MissingPermission") // Foreground service notification; POST_NOTIFICATIONS declared in manifest
        NotificationManagerCompat.from(this@BackgroundNotificationService)
            .notify(SERVICE_NOTIFICATION_ID, buildServiceNotification(webApp.name))

        // T-06-30: if the Activity is already in the foreground for this app, do not
        // create a competing GeckoSession — just keep the runtime warm.
        if (provider.activeWebViewApps.value.contains(appId)) {
            Log.d(TAG, "Activity already foreground for $appId, keeping runtime warm only")
            return
        }

        val dispatcher = provider.notificationDispatcher ?: PwaNotificationDispatcher(
            context = applicationContext,
            isGlobalNotificationsEnabled = { false },
            isDndActive = IsDndActiveUseCase(),
            saveNotification = provider.saveNotification,
            countToday = provider.countNotificationsToday,
            getCategoryById = provider.getCategoryById,
        )
        val cb = buildCallback(webApp, dispatcher)

        // GeckoRuntime.create() and GeckoSession must be called on the main thread.
        withContext(Dispatchers.Main) {
            val runtime = provider.geckoEngineManager.getRuntime()

            // WebNotificationDelegate is runtime-scoped — register it here so background
            // sessions can receive new Notification() calls when no foreground engine is active.
            runtime.setWebNotificationDelegate(object : WebNotificationDelegate {
                override fun onShowNotification(notification: WebNotification) {
                    cb.onNotificationReceived(
                        notification.title ?: "",
                        notification.text,
                        notification.imageUrl,
                        notification.tag,
                    )
                }
                override fun onCloseNotification(notification: WebNotification) = Unit
            })

            val settings = GeckoSessionSettings.Builder()
                .contextId(webApp.isolationId)
                .build()
            val session = GeckoSession(settings)
            session.open(runtime)
            NotificationDelegateFactory.attach(session, cb)
            session.loadUri(webApp.url)
            Log.i(TAG, "Background session started for ${webApp.name} (appId=$appId)")
        }
    }

    private fun buildCallback(webApp: WebApp, dispatcher: PwaNotificationDispatcher): BrowserEngineCallback =
        object : BrowserEngineCallback {
            override fun onNotificationReceived(title: String, body: String?, iconUrl: String?, tag: String?) {
                scope.launch { dispatcher.dispatch(webApp, title, body, iconUrl, tag) }
            }
            override fun onNotificationPermissionRequested(onResult: (Boolean) -> Unit) {
                // Background service: respect the stored permission; no dialog available.
                onResult(webApp.notificationPermission == NotificationPermission.GRANTED)
            }
            // No-op stubs for lifecycle callbacks irrelevant to a background-only session.
            override fun onPageStarted(url: String?) = Unit
            override fun onPageFinished(url: String?) = Unit
            override fun onProgressChanged(progress: Int) = Unit
            override fun onTitleChanged(title: String?) = Unit
            override fun onIconReceived(icon: Bitmap?) = Unit
            override fun onError(errorCode: Int, description: String) {
                Log.w(TAG, "Background session error $errorCode: $description")
            }
            override fun onSslError(error: String) {
                Log.w(TAG, "Background session SSL error: $error")
            }
            override fun onExternalLink(url: String) = Unit
            override fun onShowCustomView(view: View?, callback: Any?) = Unit
            override fun onHideCustomView() = Unit
            override fun onDownloadStart(
                url: String, userAgent: String, contentDisposition: String,
                mimeType: String, contentLength: Long,
            ) = Unit
        }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildServiceNotification(appName: String?): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(SERVICE_CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                getString(R.string.bg_notification_service_title),
                NotificationManager.IMPORTANCE_LOW,
            )
            manager.createNotificationChannel(channel)
        }

        val contentText = if (appName != null) {
            getString(R.string.bg_notification_service_text, appName)
        } else {
            getString(R.string.bg_notification_service_text_generic)
        }

        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle(getString(R.string.bg_notification_service_title))
            .setContentText(contentText)
            .setOngoing(true)
            .build()
    }
}
