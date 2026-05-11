package dev.pwaforge.core.isolation

import android.content.Context
import android.webkit.CookieManager
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

private val Context.cookieStore by preferencesDataStore(name = "pwa_cookie_store")

/**
 * API 23-32 cookie isolation: serialize/restore each PWA's cookie jar
 * around the WebView session so different PWAs never share cookies.
 *
 * Note: localStorage is already isolated by origin (same-origin policy),
 * so only cookies need explicit management on these API levels.
 */
class CookieJarManager(private val context: Context) {

    private val cookieManager = CookieManager.getInstance()

    /** Call before loading a PWA's URL. Clears the shared jar, then restores this app's cookies. */
    suspend fun restoreFor(isolationId: String) = withContext(Dispatchers.IO) {
        clearAll()
        val saved = loadSnapshot(isolationId) ?: return@withContext
        saved.split("\n").filter { it.isNotBlank() }.forEach { line ->
            val (url, cookie) = line.split("|", limit = 2).takeIf { it.size == 2 } ?: return@forEach
            cookieManager.setCookie(url, cookie)
        }
        cookieManager.flush()
    }

    /** Call when the user navigates away / closes the WebView session. Saves then clears. */
    suspend fun saveAndClearFor(isolationId: String, currentUrl: String) = withContext(Dispatchers.IO) {
        val cookies = cookieManager.getCookie(currentUrl)
        if (!cookies.isNullOrBlank()) {
            val existing = loadSnapshot(isolationId) ?: ""
            val merged = mergeCookieLines(existing, "$currentUrl|$cookies")
            saveSnapshot(isolationId, merged)
        }
        clearAll()
    }

    /** Permanently delete all cookies for a given PWA (called on app delete). */
    suspend fun deleteFor(isolationId: String) = withContext(Dispatchers.IO) {
        context.cookieStore.edit { prefs -> prefs.remove(stringPreferencesKey(isolationId)) }
    }

    private fun clearAll() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    private suspend fun loadSnapshot(isolationId: String): String? {
        val key = stringPreferencesKey(isolationId)
        return context.cookieStore.data.firstOrNull()?.get(key)
    }

    private suspend fun saveSnapshot(isolationId: String, data: String) {
        val key = stringPreferencesKey(isolationId)
        context.cookieStore.edit { it[key] = data }
    }

    private fun mergeCookieLines(existing: String, newLine: String): String {
        val lines = existing.split("\n").filter { it.isNotBlank() }.toMutableList()
        lines.removeAll { it.startsWith(newLine.substringBefore("|")) }
        lines += newLine
        return lines.joinToString("\n")
    }
}
