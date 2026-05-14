package io.shellify.app.core.security

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.passwordStore by preferencesDataStore(name = "pwa_password")

class PasswordManager(private val context: Context) {

    private val keyPasswordHash = stringPreferencesKey("password_hash")
    private val keyWipeOnFailedAttempts = booleanPreferencesKey("wipe_on_failed_attempts")
    private val keyScreenshotProtection = booleanPreferencesKey("screenshot_protection")

    val passwordHash: Flow<String?> = context.passwordStore.data.map { it[keyPasswordHash] }
    val wipeOnFailedAttempts: Flow<Boolean> = context.passwordStore.data.map { it[keyWipeOnFailedAttempts] ?: false }
    val screenshotProtection: Flow<Boolean> = context.passwordStore.data.map { it[keyScreenshotProtection] ?: false }

    suspend fun setPassword(password: String) {
        context.passwordStore.edit { it[keyPasswordHash] = hashPassword(password) }
    }

    suspend fun clearPassword() {
        context.passwordStore.edit { it.remove(keyPasswordHash) }
    }

    suspend fun setWipeOnFailedAttempts(enabled: Boolean) {
        context.passwordStore.edit { it[keyWipeOnFailedAttempts] = enabled }
    }

    suspend fun setScreenshotProtection(enabled: Boolean) {
        context.passwordStore.edit { it[keyScreenshotProtection] = enabled }
    }

    /** Restores an already-hashed password directly (used during backup restore). */
    suspend fun restorePasswordHash(hash: String) {
        context.passwordStore.edit { it[keyPasswordHash] = hash }
    }

    /** Reads a restored DataStore file and applies its contents to the live DataStore instance. */
    suspend fun reloadFromFile(restoredFile: File) {
        val tempFile = File(context.cacheDir, "tmp_password_restore.preferences_pb")
        restoredFile.copyTo(tempFile, overwrite = true)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val tempStore = PreferenceDataStoreFactory.create(scope = scope) { tempFile }
        try {
            val restored = tempStore.data.first()
            context.passwordStore.edit { current ->
                current.clear()
                @Suppress("UNCHECKED_CAST")
                restored.asMap().forEach { (key, value) ->
                    (current as MutablePreferences)[key as Preferences.Key<Any>] = value
                }
            }
        } finally {
            tempFile.delete()
            scope.cancel()
        }
    }
}
