package io.shellify.app.e2e

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.core.crypto.CryptoManager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies real Android Keystore behaviour: key generation, persistence across instances,
 * and that the database passphrase is never written as plaintext to SharedPreferences.
 */
@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    private lateinit var context: Context
    private lateinit var crypto: CryptoManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        crypto = CryptoManager(context)
    }

    @Test
    fun encrypt_decrypt_roundTrip() {
        val plain = "Shellify secure data".toByteArray()
        assertArrayEquals(plain, crypto.decrypt(crypto.encrypt(plain)))
    }

    @Test
    fun encrypt_samePlaintext_producesDifferentCiphertexts() {
        val plain = "same input".toByteArray()
        val enc1 = crypto.encrypt(plain)
        val enc2 = crypto.encrypt(plain)
        assertFalse("GCM must generate a unique IV per call", enc1.contentEquals(enc2))
        assertArrayEquals(plain, crypto.decrypt(enc1))
        assertArrayEquals(plain, crypto.decrypt(enc2))
    }

    @Test
    fun encrypt_outputSize_exceedsInputByAtLeastIvPlusTag() {
        val plain = ByteArray(64) { it.toByte() }
        assertTrue(crypto.encrypt(plain).size >= plain.size + 12 + 16)
    }

    @Test
    fun encryptString_decryptString_roundTrip() {
        val plain = "unicode 🔐 shellify"
        assertEquals(plain, crypto.decryptString(crypto.encryptString(plain)))
    }

    @Test
    fun masterKey_persistsAcrossNewInstances() {
        val plain = "key-persistence check".toByteArray()
        val encrypted = crypto.encrypt(plain)
        assertArrayEquals(plain, CryptoManager(context).decrypt(encrypted))
    }

    @Test
    fun databasePassphrase_is32Bytes() {
        assertEquals(32, crypto.databasePassphrase().size)
    }

    @Test
    fun databasePassphrase_isStableAcrossCallsAndInstances() {
        val p1 = crypto.databasePassphrase()
        assertArrayEquals(p1, crypto.databasePassphrase())
        assertArrayEquals(p1, CryptoManager(context).databasePassphrase())
    }

    @Test
    fun databasePassphrase_isNotStoredAsPlaintext() {
        val passphrase = crypto.databasePassphrase()
        val prefs = context.getSharedPreferences("shellify_crypto", Context.MODE_PRIVATE)
        val stored = prefs.getString("db_passphrase_enc", null)
        assertNotNull("Encrypted passphrase must be persisted", stored)
        assertFalse(
            "Passphrase must not appear as plaintext in SharedPreferences",
            stored!!.contains(passphrase.toString(Charsets.ISO_8859_1)),
        )
    }
}
