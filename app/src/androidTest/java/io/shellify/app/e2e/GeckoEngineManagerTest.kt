package io.shellify.app.e2e

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.core.engine.GeckoEngineManager
import io.shellify.app.core.engine.GeckoInstallState
import io.shellify.app.core.engine.GeckoNativeLoader
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Tests GeckoEngineManager state management without triggering GeckoRuntime native code
 * (getRuntime() is never called, so these run safely on emulators without Gecko libs).
 */
@RunWith(AndroidJUnit4::class)
class GeckoEngineManagerTest {

    private lateinit var context: Context
    private lateinit var manager: GeckoEngineManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearState()
        manager = GeckoEngineManager(context)
    }

    @After
    fun tearDown() = clearState()

    @Test
    fun isInstalled_returnsFalse_onFreshState() = assertFalse(manager.isInstalled())

    @Test
    fun getInstalledVersion_returnsNull_whenNotInstalled() = assertNull(manager.getInstalledVersion())

    @Test
    fun getInstalledSha256_returnsNull_whenNotInstalled() = assertNull(manager.getInstalledSha256())

    @Test
    fun getInstalledSizeMb_returnsZero_whenNotInstalled() = assertEquals(0, manager.getInstalledSizeMb())

    @Test
    fun installState_isNotInstalled_onFreshState() = assertTrue(manager.installState.value is GeckoInstallState.NotInstalled)

    @Test
    fun isInstalled_returnsTrue_whenPrefsAndSoFilePresent() {
        fakeInstall(GeckoEngineManager.GECKO_VERSION)
        assertTrue(GeckoEngineManager(context).isInstalled())
    }

    @Test
    fun installState_isInstalled_whenFakeInstallPresent() {
        fakeInstall(GeckoEngineManager.GECKO_VERSION, verified = true)
        val state = GeckoEngineManager(context).installState.value
        assertTrue(state is GeckoInstallState.Installed)
        assertTrue((state as GeckoInstallState.Installed).verified)
    }

    @Test
    fun getInstalledVersion_returnsStoredVersion() {
        fakeInstall(GeckoEngineManager.GECKO_VERSION)
        assertEquals(GeckoEngineManager.GECKO_VERSION, GeckoEngineManager(context).getInstalledVersion())
    }

    @Test
    fun updateAvailable_returnsFalse_whenNoLatestVersionKnown() {
        fakeInstall(GeckoEngineManager.GECKO_VERSION)
        assertFalse(GeckoEngineManager(context).updateAvailable)
    }

    @Test
    fun uninstall_clearsInstalledStateAndFiles() {
        fakeInstall(GeckoEngineManager.GECKO_VERSION)
        val m = GeckoEngineManager(context)
        m.uninstall()
        assertFalse(m.isInstalled())
        assertNull(m.getInstalledVersion())
        assertTrue(m.installState.value is GeckoInstallState.NotInstalled)
        assertFalse(File(context.filesDir, "gecko_engine").exists())
    }

    @Test
    fun isInstalled_returnsFalse_whenPrefSetButNoSoFile() {
        context.getSharedPreferences("gecko_engine", Context.MODE_PRIVATE)
            .edit().putBoolean("installed", true).putString("version", GeckoEngineManager.GECKO_VERSION).apply()
        assertFalse(GeckoEngineManager(context).isInstalled())
    }

    // Regression: stale 128.x libraries must not be treated as a valid install on devices that
    // require 16 KB page alignment (Android 15+), where loading a 4 KB-aligned .so would crash.
    @Test
    fun isInstalled_returnsFalse_whenVersionMismatch() {
        fakeInstall("128.0.20240704121409")
        assertFalse(GeckoEngineManager(context).isInstalled())
    }

    @Test
    fun installState_isNotInstalled_whenVersionMismatch() {
        fakeInstall("128.0.20240704121409")
        assertTrue(GeckoEngineManager(context).installState.value is GeckoInstallState.NotInstalled)
    }

    // GeckoNativeLoader must not throw or crash when the libs directory does not exist —
    // guards against a cold-start crash if prefs and disk are out of sync.
    @Test
    fun nativeLoader_doesNotCrash_whenLibsDirMissing() {
        GeckoNativeLoader.injectAndLoad(context)
    }

    // Stub .so files (wrong ELF / wrong page alignment) must produce a logged warning, not a
    // process-fatal RuntimeException. This mirrors the 4 KB-vs-16 KB alignment failure on
    // Android 15 devices, but with a deliberately malformed stub rather than a real library.
    @Test
    fun nativeLoader_doesNotCrash_whenLibsAreStubs() {
        fakeInstall(GeckoEngineManager.GECKO_VERSION)
        GeckoNativeLoader.injectAndLoad(context)
    }

    @Test
    fun applySafeBrowsing_defaultsToFalseOnFreshState() {
        assertFalse(manager.isSafeBrowsingEnabled())
    }

    @Test
    fun applySafeBrowsing_togglesInternalFlag() {
        manager.applySafeBrowsing(true)
        assertTrue(manager.isSafeBrowsingEnabled())

        manager.applySafeBrowsing(false)
        assertFalse(manager.isSafeBrowsingEnabled())
    }

    private fun fakeInstall(version: String, verified: Boolean = false) {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        File(context.filesDir, "gecko_engine/lib/$abi").mkdirs()
        File(context.filesDir, "gecko_engine/lib/$abi/libxul.so").writeBytes(ByteArray(64))
        context.getSharedPreferences("gecko_engine", Context.MODE_PRIVATE).edit()
            .putBoolean("installed", true).putString("version", version)
            .putBoolean("sha256_verified", verified).apply()
    }

    private fun clearState() {
        File(context.filesDir, "gecko_engine").deleteRecursively()
        context.getSharedPreferences("gecko_engine", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
