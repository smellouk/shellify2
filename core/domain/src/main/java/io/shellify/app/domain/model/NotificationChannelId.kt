package io.shellify.app.domain.model

object NotificationChannelId {
    const val PREFIX = "pwa_notifications_"
    fun forApp(isolationId: String) = "$PREFIX$isolationId"
}
