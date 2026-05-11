package dev.pwaforge.core.security

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
}
