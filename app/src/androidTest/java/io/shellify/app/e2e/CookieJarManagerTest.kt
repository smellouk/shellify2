package io.shellify.app.e2e

import android.content.Context
import android.webkit.CookieManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.core.crypto.CryptoManager
import io.shellify.app.core.isolation.CookieJarManager
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val ISO_A = "test-isolation-alpha"
private const val ISO_B = "test-isolation-beta"
private const val TEST_URL = "https://example.shellify.test"

@RunWith(AndroidJUnit4::class)
class CookieJarManagerTest {

    private lateinit var manager: CookieJarManager

    @Before
    fun setUp() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        manager = CookieJarManager(context, CryptoManager(context))
        manager.deleteFor(ISO_A)
        manager.deleteFor(ISO_B)
        manager.clearAll()
    }

    @After
    fun tearDown() = runTest {
        manager.deleteFor(ISO_A)
        manager.deleteFor(ISO_B)
        manager.clearAll()
    }

    @Test
    fun saveAndClearFor_thenRestoreFor_cookieIsPresent() = runTest {
        CookieManager.getInstance().setCookie(TEST_URL, "session=abc123")
        manager.saveAndClearFor(ISO_A, setOf(TEST_URL))
        manager.restoreFor(ISO_A)
        val restored = CookieManager.getInstance().getCookie(TEST_URL)
        assertNotNull(restored)
        assertTrue(restored!!.contains("session=abc123"))
    }

    @Test
    fun saveAndClearFor_clearsSharedCookieJar() = runTest {
        CookieManager.getInstance().setCookie(TEST_URL, "session=abc123")
        manager.saveAndClearFor(ISO_A, setOf(TEST_URL))
        assertTrue(CookieManager.getInstance().getCookie(TEST_URL).isNullOrBlank())
    }

    @Test
    fun differentIsolationIds_haveIndependentCookies() = runTest {
        CookieManager.getInstance().setCookie(TEST_URL, "session=alpha")
        manager.saveAndClearFor(ISO_A, setOf(TEST_URL))

        CookieManager.getInstance().setCookie(TEST_URL, "session=beta")
        manager.saveAndClearFor(ISO_B, setOf(TEST_URL))

        manager.restoreFor(ISO_A)
        val cookiesA = CookieManager.getInstance().getCookie(TEST_URL)
        assertTrue(cookiesA?.contains("session=alpha") == true)
        assertFalse(cookiesA?.contains("session=beta") == true)
        manager.clearAll()

        manager.restoreFor(ISO_B)
        val cookiesB = CookieManager.getInstance().getCookie(TEST_URL)
        assertTrue(cookiesB?.contains("session=beta") == true)
        assertFalse(cookiesB?.contains("session=alpha") == true)
    }

    @Test
    fun deleteFor_removesStoredCookies() = runTest {
        CookieManager.getInstance().setCookie(TEST_URL, "session=abc123")
        manager.saveAndClearFor(ISO_A, setOf(TEST_URL))
        manager.deleteFor(ISO_A)
        manager.restoreFor(ISO_A)
        assertTrue(CookieManager.getInstance().getCookie(TEST_URL).isNullOrBlank())
    }

    @Test
    fun exportAll_returnsPlaintextDecryptedValues() = runTest {
        CookieManager.getInstance().setCookie(TEST_URL, "session=abc123")
        manager.saveAndClearFor(ISO_A, setOf(TEST_URL))
        val exported = manager.exportAll()
        assertTrue(exported.containsKey(ISO_A))
        assertTrue(exported[ISO_A]!!.contains(TEST_URL))
    }

    @Test
    fun importAll_thenExportAll_roundTrips() = runTest {
        manager.importAll(mapOf(ISO_A to "$TEST_URL|session=abc123"))
        val exported = manager.exportAll()
        assertTrue(exported.containsKey(ISO_A))
        assertTrue(exported[ISO_A]!!.contains("session=abc123"))
    }

    @Test
    fun exportAll_returnsEmpty_whenNothingStored() = runTest {
        assertNull(manager.exportAll()[ISO_A])
    }
}
