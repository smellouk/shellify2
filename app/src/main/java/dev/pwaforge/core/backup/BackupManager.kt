package dev.pwaforge.core.backup

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import dev.pwaforge.core.isolation.CookieJarManager
import dev.pwaforge.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val database: AppDatabase,
    private val cookieJarManager: CookieJarManager,
) {

    // ── Public API ────────────────────────────────────────────────────────────

    /** Creates an encrypted .pwab file inside [directoryUri] (SAF tree URI). Returns the filename. */
    suspend fun backup(password: String, directoryUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val zipBytes = buildZipArchive()
            val encrypted = BackupCrypto.encrypt(zipBytes, password)
            val filename = "pwaforge_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pwab"
            val dir = DocumentFile.fromTreeUri(context, directoryUri)
                ?: error("Cannot open backup directory")
            val file = dir.createFile("application/octet-stream", filename)
                ?: error("Cannot create backup file in selected directory")
            context.contentResolver.openOutputStream(file.uri)!!.use { it.write(encrypted) }
            filename
        }
    }

    /** Decrypts and restores a .pwab file from [sourceUri]. Requires app restart to fully apply. */
    suspend fun restore(password: String, sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val encrypted = context.contentResolver.openInputStream(sourceUri)!!.readBytes()
            val zipBytes = try {
                BackupCrypto.decrypt(encrypted, password)
            } catch (e: Exception) {
                error("Wrong password or corrupted backup")
            }
            applyZipArchive(zipBytes)
        }
    }

    // ── Build archive ─────────────────────────────────────────────────────────

    private suspend fun buildZipArchive(): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->

            // Metadata
            zip.entry("meta.json") {
                JSONObject().apply {
                    put("version", 1)
                    put("timestamp", System.currentTimeMillis())
                    put("package", context.packageName)
                    put("apiLevel", Build.VERSION.SDK_INT)
                }.toString().toByteArray()
            }

            // Database — portable SQL dump (works across devices with different SQLCipher keys)
            zip.entry("db/dump.sql") { exportDatabaseSql().toByteArray(Charsets.UTF_8) }

            // Icons
            File(context.filesDir, "icons").walkSafe { file, relative ->
                zip.entry("icons/$relative") { file.readBytes() }
            }

            // All DataStore preference files (plain protobuf, no device-specific encryption)
            val datastoreDir = File(context.filesDir.parent!!, "datastore")
            listOf(
                "pwa_theme.preferences_pb",
                "pwa_password.preferences_pb",
                "pwa_backup.preferences_pb",
            ).forEach { name ->
                val f = File(datastoreDir, name)
                if (f.exists()) zip.entry("datastore/$name") { f.readBytes() }
            }

            // Cookies — decrypted for cross-device portability, re-encrypted by BackupCrypto
            val cookies = cookieJarManager.exportAll()
            if (cookies.isNotEmpty()) {
                zip.entry("cookies.json") {
                    JSONObject(cookies).toString().toByteArray(Charsets.UTF_8)
                }
            }

            // WebView profile directories (API 33+) — best-effort binary copy
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val profilesDir = File(context.filesDir.parent!!, "app_webview/profiles")
                profilesDir.walkSafe { file, relative ->
                    zip.entry("webview/$relative") { file.readBytes() }
                }
            }
        }
        return out.toByteArray()
    }

    // ── Apply archive ─────────────────────────────────────────────────────────

    private suspend fun applyZipArchive(zipBytes: ByteArray) {
        val cookieMap = mutableMapOf<String, String>()

        ZipInputStream(zipBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val bytes = zip.readBytes()
                    when {
                        entry.name == "db/dump.sql" ->
                            restoreDatabaseSql(bytes.toString(Charsets.UTF_8))

                        entry.name.startsWith("icons/") -> {
                            val name = entry.name.removePrefix("icons/")
                            if (name.isNotBlank()) {
                                val dir = File(context.filesDir, "icons").also { it.mkdirs() }
                                File(dir, name).writeBytes(bytes)
                            }
                        }

                        entry.name.startsWith("datastore/") -> {
                            val name = entry.name.removePrefix("datastore/")
                            if (name.isNotBlank()) {
                                val dir = File(context.filesDir.parent!!, "datastore").also { it.mkdirs() }
                                File(dir, name).writeBytes(bytes)
                            }
                        }

                        entry.name == "cookies.json" -> {
                            val json = JSONObject(bytes.toString(Charsets.UTF_8))
                            json.keys().forEach { key -> cookieMap[key] = json.getString(key) }
                        }

                        entry.name.startsWith("webview/") &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                            val relative = entry.name.removePrefix("webview/")
                            if (relative.isNotBlank()) {
                                val target = File(context.filesDir.parent!!, "app_webview/$relative")
                                target.parentFile?.mkdirs()
                                target.writeBytes(bytes)
                            }
                        }
                    }
                }
                entry = zip.nextEntry
            }
        }

        if (cookieMap.isNotEmpty()) cookieJarManager.importAll(cookieMap)
    }

    // ── Database helpers ──────────────────────────────────────────────────────

    private fun exportDatabaseSql(): String {
        val sb = StringBuilder()
        val db = database.openHelper.readableDatabase
        listOf("categories", "web_apps").forEach { table ->
            val cursor = db.query("SELECT * FROM `$table`")
            cursor.use {
                val cols = it.columnNames
                while (it.moveToNext()) {
                    val values = cols.indices.map { i ->
                        val v = it.getString(i)
                        if (v == null) "NULL" else "'${v.replace("'", "''")}'"
                    }
                    sb.appendLine(
                        "INSERT OR REPLACE INTO `$table` " +
                        "(${cols.joinToString(",") { "`$it`" }}) " +
                        "VALUES (${values.joinToString(",")});"
                    )
                }
            }
        }
        return sb.toString()
    }

    private fun restoreDatabaseSql(sql: String) {
        val db = database.openHelper.writableDatabase
        db.beginTransaction()
        try {
            sql.lineSequence().filter { it.isNotBlank() }.forEach { stmt ->
                runCatching { db.execSQL(stmt) }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private inline fun ZipOutputStream.entry(name: String, block: () -> ByteArray) {
        putNextEntry(ZipEntry(name))
        write(block())
        closeEntry()
    }

    private inline fun File.walkSafe(crossinline action: (file: File, relative: String) -> Unit) {
        if (!exists()) return
        walkTopDown().filter { it.isFile }.forEach { file ->
            runCatching { action(file, file.relativeTo(this).path) }
        }
    }
}
