package dev.pwaforge.core.isolation

import android.content.Context
import android.webkit.CookieManager
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.pwaforge.core.crypto.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

private val Context.cookieStore by preferencesDataStore(name = "pwa_cookie_store")

/**
 * API 23-32 cookie isolation: serialize/restore each PWA's cookie jar
 * around the WebView session so different PWAs never share cookies.
 *
 * All cookie blobs are AES-256-GCM encrypted before being written to DataStore
 * and decrypted only when the corresponding PWA is about to be launched.
 */
class CookieJarManager(
    private val context: Context,
    private val crypto: CryptoManager,
) {
    private val cookieManager = CookieManager.getInstance()

    /** Call before loading a PWA's URL — clears the shared jar, restores this app's cookies. */
    suspend fun restoreFor(isolationId: String) = withContext(Dispatchers.IO) {
        clearAll()
        val decrypted = loadDecrypted(isolationId) ?: return@withContext
        decrypted.split("\n").filter { it.isNotBlank() }.forEach { line ->
            val (url, cookie) = line.split("|", limit = 2).takeIf { it.size == 2 } ?: return@forEach
            cookieManager.setCookie(url, cookie)
        }
        cookieManager.flush()
    }

    /** Call when the user closes the WebView — saves (encrypted) then clears. */
    suspend fun saveAndClearFor(isolationId: String, currentUrl: String) = withContext(Dispatchers.IO) {
        val cookies = cookieManager.getCookie(currentUrl)
        if (!cookies.isNullOrBlank()) {
            val existing = loadDecrypted(isolationId) ?: ""
            val merged = mergeCookieLines(existing, "$currentUrl|$cookies")
            saveEncrypted(isolationId, merged)
        }
        clearAll()
    }

    /** Permanently wipe all cookies for a given PWA (called on app delete). */
    suspend fun deleteFor(isolationId: String) = withContext(Dispatchers.IO) {
        context.cookieStore.edit { prefs -> prefs.remove(stringPreferencesKey(isolationId)) }
    }

    // ---------- internal ----------

    private fun clearAll() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    private suspend fun loadDecrypted(isolationId: String): String? {
        val key = stringPreferencesKey(isolationId)
        val encrypted = context.cookieStore.data.firstOrNull()?.get(key) ?: return null
        return runCatching { crypto.decryptString(encrypted) }.getOrNull()
    }

    private suspend fun saveEncrypted(isolationId: String, plaintext: String) {
        val key = stringPreferencesKey(isolationId)
        val encrypted = crypto.encryptString(plaintext)
        context.cookieStore.edit { it[key] = encrypted }
    }

    private fun mergeCookieLines(existing: String, newLine: String): String {
        val prefix = newLine.substringBefore("|")
        val lines = existing.split("\n").filter { it.isNotBlank() && !it.startsWith(prefix) }.toMutableList()
        lines += newLine
        return lines.joinToString("\n")
    }
}
