package dev.pwaforge.core.pwa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Downloads an icon (from PWA manifest or favicon fallback) and stores it
 * in the app's private files directory.
 * Inspired by AppForge's FaviconFetcher.
 */
class FaviconFetcher(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build(),
) {
    /**
     * Downloads [iconUrl], saves to internal storage, and returns the local path.
     * Falls back to /favicon.ico from [siteUrl] if [iconUrl] is null.
     */
    suspend fun fetch(iconUrl: String?, siteUrl: String, isolationId: String): String? =
        withContext(Dispatchers.IO) {
            val candidates = buildList {
                if (iconUrl != null) add(iconUrl)
                val origin = extractOrigin(siteUrl)
                add("$origin/favicon.ico")
                add("$origin/apple-touch-icon.png")
                add("$origin/apple-touch-icon-precomposed.png")
            }

            for (url in candidates) {
                val bitmap = downloadBitmap(url) ?: continue
                val file = saveIcon(bitmap, isolationId)
                if (file != null) return@withContext file.absolutePath
            }
            null
        }

    private fun downloadBitmap(url: String): Bitmap? = runCatching {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@runCatching null
            val bytes = response.body?.bytes() ?: return@runCatching null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }.getOrNull()

    private fun saveIcon(bitmap: Bitmap, isolationId: String): File? = runCatching {
        val dir = File(context.filesDir, "icons").also { it.mkdirs() }
        val file = File(dir, "$isolationId.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    }.getOrNull()

    /** Copies a content:// URI picked by the user into the icons directory and returns the path. */
    suspend fun saveFromContentUri(uri: android.net.Uri, isolationId: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, "icons").also { it.mkdirs() }
                val file = File(dir, "$isolationId.png")
                context.contentResolver.openInputStream(uri)?.use { it.copyTo(file.outputStream()) }
                file.absolutePath
            }.getOrNull()
        }

    private fun extractOrigin(url: String): String {
        val proto = if (url.startsWith("https")) "https" else "http"
        val host = url.removePrefix("https://").removePrefix("http://").substringBefore('/')
        return "$proto://$host"
    }
}
