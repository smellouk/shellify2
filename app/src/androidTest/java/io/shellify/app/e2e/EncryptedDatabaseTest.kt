package io.shellify.app.e2e

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.core.crypto.CryptoManager
import io.shellify.app.data.local.AppDatabase
import io.shellify.app.data.local.entity.WebAppEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that the SQLCipher-encrypted Room database is actually encrypted on disk,
 * that data survives close/reopen cycles with the correct passphrase, and that a wrong
 * passphrase throws on the first query.
 *
 * Uses a dedicated test DB name to avoid touching the app's production singleton.
 */
@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {

    private lateinit var context: Context
    private lateinit var crypto: CryptoManager
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        System.loadLibrary("sqlcipher")
        context = ApplicationProvider.getApplicationContext()
        crypto = CryptoManager(context)
        context.deleteDatabase(TEST_DB)
        db = buildDb(crypto.databasePassphrase())
    }

    @After
    fun tearDown() {
        if (db.isOpen) db.close()
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun database_opensSuccessfully_withCryptoManagerPassphrase() = runTest {
        assertEquals(0, db.webAppDao().getAll().first().size)
    }

    @Test
    fun database_writtenData_isRetrievableAfterReopen() = runTest {
        db.webAppDao().insert(entity("Persist Test", "https://persist.test"))
        db.close()
        val db2 = buildDb(crypto.databasePassphrase())
        try {
            val apps = db2.webAppDao().getAll().first()
            assertEquals(1, apps.size)
            assertEquals("Persist Test", apps.first().name)
        } finally {
            db2.close()
        }
    }

    @Test
    fun databaseFile_doesNotContainInsertedStringAsPlaintext() = runTest {
        val canary = "SHELLIFY_SECRET_CANARY_XYZ_987"
        db.webAppDao().insert(entity(canary, "https://secret.test"))
        db.close()
        val raw = context.getDatabasePath(TEST_DB).readBytes().toString(Charsets.ISO_8859_1)
        assertTrue("Database file must exist", context.getDatabasePath(TEST_DB).exists())
        assertFalse("Database file must not contain inserted data as plaintext", raw.contains(canary))
    }

    @Test
    fun wrongPassphrase_throwsOnFirstQuery() = runTest {
        db.webAppDao().insert(entity("App", "https://example.com"))
        db.close()
        val wrongDb = buildDb(ByteArray(32) { 0x42 })
        val threw = runCatching { wrongDb.webAppDao().getAll().first() }.isFailure
        wrongDb.close()
        assertTrue("Wrong passphrase must throw on first query", threw)
    }

    @Test
    fun database_supportsMultipleInserts_andFlowEmitsAll() = runTest {
        repeat(5) { i -> db.webAppDao().insert(entity("App $i", "https://app$i.test")) }
        assertEquals(5, db.webAppDao().getAll().first().size)
    }

    private fun buildDb(passphrase: ByteArray) =
        Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .openHelperFactory(SupportOpenHelperFactory(passphrase))
            .allowMainThreadQueries()
            .build()

    private fun entity(name: String, url: String) =
        WebAppEntity(name = name, url = url, isolationId = "test-iso")

    companion object {
        private const val TEST_DB = "test_shellify_encrypted.db"
    }
}
