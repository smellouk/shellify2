package dev.pwaforge.data.local.converter

import androidx.room.TypeConverter
import dev.pwaforge.domain.model.IconSource

class IconSourceConverter {
    @TypeConverter
    fun fromIconSource(source: IconSource?): String? = source?.toJson()

    @TypeConverter
    fun toIconSource(json: String?): IconSource? = IconSource.fromJson(json)
}
