package io.shellify.app.data.local.migration

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.util.Pair
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement

/**
 * Minimal stub of [SupportSQLiteDatabase] used exclusively in unit tests for [MIGRATION_3_4].
 *
 * Only [execSQL] is implemented. All other methods throw [UnsupportedOperationException].
 * This lets us capture the SQL emitted by the migration without requiring an Android runtime.
 */
class StubSupportSQLiteDatabase(
    private val recordedStatements: MutableList<String>,
) : SupportSQLiteDatabase {

    override fun execSQL(sql: String) {
        recordedStatements.add(sql)
    }

    override fun execSQL(sql: String, bindArgs: Array<out Any?>) {
        recordedStatements.add(sql)
    }

    // ── Kotlin property overrides ──────────────────────────────────────────────

    override val maximumSize: Long get() = 0L
    override var pageSize: Long = 0L

    // ── Not used by migrations — provide no-op or throw ───────────────────────

    override fun compileStatement(sql: String): SupportSQLiteStatement =
        throw UnsupportedOperationException("compileStatement not supported in stub")

    override fun beginTransaction() = Unit
    override fun beginTransactionNonExclusive() = Unit
    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) = Unit
    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) = Unit
    override fun endTransaction() = Unit
    override fun setTransactionSuccessful() = Unit
    override fun inTransaction(): Boolean = false
    override fun yieldIfContendedSafely(): Boolean = false
    override fun yieldIfContendedSafely(sleepAfterYieldDelayMillis: Long): Boolean = false
    override var version: Int = 0
    override val isDbLockedByCurrentThread: Boolean get() = false
    override fun setMaximumSize(numBytes: Long): Long = 0L
    override val isReadOnly: Boolean get() = false
    override val isOpen: Boolean get() = true
    override val isDatabaseIntegrityOk: Boolean get() = true
    override val path: String get() = ":memory:"
    override val attachedDbs: MutableList<Pair<String, String>>? get() = null
    override val isWriteAheadLoggingEnabled: Boolean get() = false

    override fun query(query: String): Cursor =
        throw UnsupportedOperationException("query not supported in stub")
    override fun query(query: String, bindArgs: Array<out Any?>): Cursor =
        throw UnsupportedOperationException("query not supported in stub")
    override fun query(query: SupportSQLiteQuery): Cursor =
        throw UnsupportedOperationException("query not supported in stub")
    override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal?): Cursor =
        throw UnsupportedOperationException("query not supported in stub")
    override fun insert(table: String, conflictAlgorithm: Int, values: ContentValues): Long = -1L
    override fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?): Int = 0
    override fun update(table: String, conflictAlgorithm: Int, values: ContentValues, whereClause: String?, whereArgs: Array<out Any?>?): Int = 0
    override fun needUpgrade(newVersion: Int): Boolean = false
    override fun setLocale(locale: java.util.Locale) = Unit
    override fun setMaxSqlCacheSize(cacheSize: Int) = Unit
    override fun setForeignKeyConstraintsEnabled(enabled: Boolean) = Unit
    override fun enableWriteAheadLogging(): Boolean = false
    override fun disableWriteAheadLogging() = Unit
    override fun close() = Unit
}
