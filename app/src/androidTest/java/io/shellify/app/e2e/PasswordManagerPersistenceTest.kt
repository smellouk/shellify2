package io.shellify.app.e2e

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.core.security.PasswordManager
import io.shellify.app.core.security.verifyPassword
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val APP_ID = 99_001L

/**
 * Verifies PasswordManager DataStore persistence: password hash format, failed-attempt
 * tracking, and flag storage — all backed by real on-device DataStore I/O.
 */
@RunWith(AndroidJUnit4::class)
class PasswordManagerPersistenceTest {

    private lateinit var manager: PasswordManager

    @Before
    fun setUp() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        manager = PasswordManager(context)
        manager.clearPassword()
        manager.clearFailedAttempts(APP_ID)
    }

    @Test
    fun setPassword_storesNonNullHash() = runTest {
        manager.setPassword("correct-horse-battery-staple")
        assertNotNull(manager.passwordHash.first())
    }

    @Test
    fun setPassword_hashHasV2PbkdfFormat() = runTest {
        manager.setPassword("any-password")
        assertTrue(manager.passwordHash.first()!!.startsWith("v2:"))
    }

    @Test
    fun setPassword_hashVerifiesWithCorrectPassword() = runTest {
        manager.setPassword("super-secret-123")
        assertTrue(verifyPassword("super-secret-123", manager.passwordHash.first()!!))
    }

    @Test
    fun setPassword_hashRejectsWrongPassword() = runTest {
        manager.setPassword("correct-password")
        assertTrue(!verifyPassword("wrong-password", manager.passwordHash.first()!!))
    }

    @Test
    fun clearPassword_passwordHashBecomesNull() = runTest {
        manager.setPassword("will-be-cleared")
        manager.clearPassword()
        assertNull(manager.passwordHash.first())
    }

    @Test
    fun restorePasswordHash_restoresRawHash() = runTest {
        manager.setPassword("original")
        val hash = manager.passwordHash.first()!!
        manager.clearPassword()
        manager.restorePasswordHash(hash)
        assertEquals(hash, manager.passwordHash.first())
    }

    @Test
    fun recordFailedAttempt_incrementsCount() = runTest {
        assertEquals(1, manager.recordFailedAttempt(APP_ID))
        assertEquals(2, manager.recordFailedAttempt(APP_ID))
        assertEquals(2, manager.getFailedAttempts(APP_ID))
    }

    @Test
    fun clearFailedAttempts_resetsToZero() = runTest {
        manager.recordFailedAttempt(APP_ID)
        manager.recordFailedAttempt(APP_ID)
        manager.clearFailedAttempts(APP_ID)
        assertEquals(0, manager.getFailedAttempts(APP_ID))
    }

    @Test
    fun failedAttempts_areIndependentPerAppId() = runTest {
        val otherId = APP_ID + 1
        manager.recordFailedAttempt(APP_ID)
        manager.recordFailedAttempt(APP_ID)
        manager.recordFailedAttempt(otherId)
        assertEquals(2, manager.getFailedAttempts(APP_ID))
        assertEquals(1, manager.getFailedAttempts(otherId))
        manager.clearFailedAttempts(otherId)
    }

    @Test
    fun setWipeOnFailedAttempts_reflectedInFlow() = runTest {
        manager.setWipeOnFailedAttempts(true)
        assertTrue(manager.wipeOnFailedAttempts.first())
        manager.setWipeOnFailedAttempts(false)
    }

    @Test
    fun setScreenshotProtection_reflectedInFlow() = runTest {
        manager.setScreenshotProtection(true)
        assertTrue(manager.screenshotProtection.first())
        manager.setScreenshotProtection(false)
    }
}
