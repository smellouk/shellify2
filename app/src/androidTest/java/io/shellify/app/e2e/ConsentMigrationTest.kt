package io.shellify.app.e2e

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.core.theme.ThemeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies consent version migration using real DataStore I/O — no mocking.
 *
 * Three storage states exist on real devices:
 *   - No keys at all (fresh install)             → consentVersion == 0
 *   - consent_given=true, no consent_version     → consentVersion == 1 (legacy migration)
 *   - consent_version present                    → consentVersion == stored value
 */
@RunWith(AndroidJUnit4::class)
class ConsentMigrationTest {

    private lateinit var manager: ThemeManager

    @Before
    fun setUp() = runTest {
        manager = ThemeManager(ApplicationProvider.getApplicationContext<Context>())
        manager.clearConsentForTesting()
    }

    // ── Fresh install ─────────────────────────────────────────────────────────

    @Test
    fun freshInstall_consentVersionIsZero() = runTest {
        assertEquals(0, manager.consentVersion.first())
    }

    @Test
    fun freshInstall_consentGivenIsFalse() = runTest {
        assertTrue(!manager.consentGiven.first())
    }

    // ── Legacy migration ──────────────────────────────────────────────────────

    @Test
    fun legacyConsent_consentVersionMigratesTo1() = runTest {
        manager.seedLegacyConsentForTesting()
        assertEquals(1, manager.consentVersion.first())
    }

    @Test
    fun legacyConsent_consentGivenRemainsTrue() = runTest {
        manager.seedLegacyConsentForTesting()
        assertTrue(manager.consentGiven.first())
    }

    @Test
    fun legacyConsent_versionIsLessThanCurrent() = runTest {
        manager.seedLegacyConsentForTesting()
        assertTrue(manager.consentVersion.first() < ThemeManager.CURRENT_CONSENT_VERSION)
    }

    // ── First-run acceptance ──────────────────────────────────────────────────

    @Test
    fun setConsentGiven_consentGivenIsTrue() = runTest {
        manager.setConsentGiven()
        assertTrue(manager.consentGiven.first())
    }

    @Test
    fun setConsentGiven_storesCurrentConsentVersion() = runTest {
        manager.setConsentGiven()
        assertEquals(ThemeManager.CURRENT_CONSENT_VERSION, manager.consentVersion.first())
    }

    // ── Update acceptance ─────────────────────────────────────────────────────

    @Test
    fun setConsentVersion_updatesStoredVersion() = runTest {
        manager.seedLegacyConsentForTesting()
        manager.setConsentVersion(ThemeManager.CURRENT_CONSENT_VERSION)
        assertEquals(ThemeManager.CURRENT_CONSENT_VERSION, manager.consentVersion.first())
    }

    @Test
    fun setConsentVersion_doesNotClearConsentGiven() = runTest {
        manager.seedLegacyConsentForTesting()
        manager.setConsentVersion(ThemeManager.CURRENT_CONSENT_VERSION)
        assertTrue(manager.consentGiven.first())
    }

    // ── Version persistence ───────────────────────────────────────────────────

    @Test
    fun consentVersion_persistsAfterSecondRead() = runTest {
        manager.setConsentGiven()
        val first = manager.consentVersion.first()
        val second = manager.consentVersion.first()
        assertEquals(first, second)
    }
}
