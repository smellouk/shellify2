package io.shellify.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE web_apps ADD COLUMN show_control_center INTEGER NOT NULL DEFAULT 1"
        )
    }
}
