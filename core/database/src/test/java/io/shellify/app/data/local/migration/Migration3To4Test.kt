package io.shellify.app.data.local.migration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for MIGRATION_3_4 verifying the SQL statement and migration version numbers.
 *
 * Full end-to-end Room migration testing (inserting rows, querying after migration) is covered by
 * DatabaseMigrationTest in androidTest, which uses MigrationTestHelper with SQLCipher.
 *
 * These tests validate the migration object's contract — version bounds and SQL correctness —
 * without requiring an Android runtime or instrumentation.
 */
class Migration3To4Test {

    @Test
    fun `migration starts at version 3`() {
        assertEquals(3, MIGRATION_3_4.startVersion)
    }

    @Test
    fun `migration ends at version 4`() {
        assertEquals(4, MIGRATION_3_4.endVersion)
    }

    @Test
    fun `migration adds swipe_to_refresh_enabled column to web_apps`() {
        // Capture the SQL executed by inspecting the migration object's class name and version.
        // The SQL is validated here by verifying the migrate() method body via a test-only
        // SupportSQLiteDatabase stub that records executed statements.
        val recordedStatements = mutableListOf<String>()
        val stubDb = StubSupportSQLiteDatabase(recordedStatements)

        MIGRATION_3_4.migrate(stubDb)

        assertTrue(
            "Migration must execute exactly one SQL statement",
            recordedStatements.size == 1,
        )
        val sql = recordedStatements[0]
        assertTrue(
            "SQL must target the web_apps table",
            sql.contains("web_apps", ignoreCase = true),
        )
        assertTrue(
            "SQL must add swipe_to_refresh_enabled column",
            sql.contains("swipe_to_refresh_enabled", ignoreCase = true),
        )
        assertTrue(
            "Column must be NOT NULL INTEGER",
            sql.contains("INTEGER NOT NULL", ignoreCase = true),
        )
        assertTrue(
            "Column must default to 1 (enabled for all existing rows)",
            sql.contains("DEFAULT 1", ignoreCase = true),
        )
    }

    @Test
    fun `migration sql is an ALTER TABLE ADD COLUMN statement`() {
        val recordedStatements = mutableListOf<String>()
        val stubDb = StubSupportSQLiteDatabase(recordedStatements)

        MIGRATION_3_4.migrate(stubDb)

        val sql = recordedStatements[0]
        assertTrue(
            "SQL must be an ALTER TABLE statement",
            sql.trim().startsWith("ALTER TABLE", ignoreCase = true),
        )
        assertTrue(
            "SQL must use ADD COLUMN",
            sql.contains("ADD COLUMN", ignoreCase = true),
        )
    }
}
