package io.shellify.app.domain.model

data class PwaNotification(
    val id: Long = 0,
    val appId: Long,
    val title: String,
    val body: String? = null,
    val iconUrl: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false,
)
