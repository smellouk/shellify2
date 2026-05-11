package dev.pwaforge.core.backup

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.pwaforge.core.crypto.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class BackupSchedule { NONE, DAILY, WEEKLY }

private val Context.backupStore by preferencesDataStore(name = "pwa_backup")

class BackupSettings(private val context: Context, private val crypto: CryptoManager) {

    private val keyEnabled = booleanPreferencesKey("enabled")
    private val keyPasswordEnc = stringPreferencesKey("password_enc")
    private val keyDirectoryUri = stringPreferencesKey("directory_uri")
    private val keySchedule = stringPreferencesKey("schedule")
    private val keyLastBackup = longPreferencesKey("last_backup")

    val enabled: Flow<Boolean> = context.backupStore.data.map { it[keyEnabled] ?: false }
    val hasPassword: Flow<Boolean> = context.backupStore.data.map { it[keyPasswordEnc] != null }
    val directoryUri: Flow<String?> = context.backupStore.data.map { it[keyDirectoryUri] }
    val schedule: Flow<BackupSchedule> = context.backupStore.data.map {
        runCatching { BackupSchedule.valueOf(it[keySchedule] ?: "") }.getOrDefault(BackupSchedule.NONE)
    }
    val lastBackupTime: Flow<Long> = context.backupStore.data.map { it[keyLastBackup] ?: 0L }

    suspend fun setEnabled(v: Boolean) = context.backupStore.edit { it[keyEnabled] = v }
    suspend fun setDirectoryUri(uri: String) = context.backupStore.edit { it[keyDirectoryUri] = uri }
    suspend fun setSchedule(s: BackupSchedule) = context.backupStore.edit { it[keySchedule] = s.name }
    suspend fun setLastBackupTime(t: Long) = context.backupStore.edit { it[keyLastBackup] = t }

    suspend fun setPassword(password: String) {
        val encrypted = crypto.encryptString(password)
        context.backupStore.edit { it[keyPasswordEnc] = encrypted }
    }

    suspend fun clearPassword() {
        context.backupStore.edit { it.remove(keyPasswordEnc) }
    }

    suspend fun getPassword(): String? {
        val enc = context.backupStore.data.first()[keyPasswordEnc] ?: return null
        return runCatching { crypto.decryptString(enc) }.getOrNull()
    }
}
