package io.shellify.app.core.iconpack

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class SimpleIconsReader(private val context: Context) {

    private val dataFile: File
        get() = File(context.filesDir, "icon_packs/simple_icons.json")

    suspend fun readAll(): List<SimpleIconEntry> = withContext(Dispatchers.IO) {
        if (!dataFile.exists()) return@withContext emptyList()
        runCatching {
            val array = JSONArray(dataFile.readText())
            val result = mutableListOf<SimpleIconEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title").takeIf { it.isNotBlank() } ?: continue
                val slug = obj.optString("slug").takeIf { it.isNotBlank() } ?: slugify(title)
                val hex = obj.optString("hex").takeIf { it.isNotBlank() } ?: "000000"
                result.add(SimpleIconEntry(title = title, slug = slug, hex = hex))
            }
            result
        }.getOrDefault(emptyList())
    }

    // Matches simple-icons' own slug derivation algorithm
    private fun slugify(title: String): String =
        title.lowercase()
            .replace("+", "plus")
            .replace(".", "dot")
            .replace(" ", "")
            .replace(Regex("[^a-z0-9]"), "")
}
