package io.shellify.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = WebAppEntity::class,
            parentColumns = ["id"],
            childColumns = ["app_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("app_id"), Index("timestamp")],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "app_id") val appId: Long,
    val title: String,
    val body: String? = null,
    @ColumnInfo(name = "icon_url") val iconUrl: String? = null,
    val timestamp: Long,
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
)
