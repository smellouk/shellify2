package dev.pwaforge.core.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.passwordStore by preferencesDataStore(name = "pwa_password")

class PasswordManager(private val context: Context) {

    private val keyPasswordHash = stringPreferencesKey("password_hash")

    val passwordHash: Flow<String?> = context.passwordStore.data.map { it[keyPasswordHash] }

    suspend fun setPassword(password: String) {
        context.passwordStore.edit { it[keyPasswordHash] = hashPassword(password) }
    }

    suspend fun clearPassword() {
        context.passwordStore.edit { it.remove(keyPasswordHash) }
    }
}
