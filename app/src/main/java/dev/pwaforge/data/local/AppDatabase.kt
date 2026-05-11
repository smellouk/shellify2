package dev.pwaforge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.pwaforge.data.local.dao.CategoryDao
import dev.pwaforge.data.local.dao.WebAppDao
import dev.pwaforge.data.local.entity.CategoryEntity
import dev.pwaforge.data.local.entity.WebAppEntity

@Database(
    entities = [WebAppEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webAppDao(): WebAppDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pwaforge.db",
                ).build().also { instance = it }
            }
    }
}
