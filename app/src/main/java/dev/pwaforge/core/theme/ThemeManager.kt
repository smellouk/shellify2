package dev.pwaforge.core.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore by preferencesDataStore(name = "pwa_theme")

class ThemeManager(private val context: Context) {

    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyDynamicColor = booleanPreferencesKey("dynamic_color")

    val themeMode: Flow<ThemeMode> = context.themeStore.data.map { prefs ->
        runCatching { ThemeMode.valueOf(prefs[keyThemeMode] ?: "") }.getOrDefault(ThemeMode.SYSTEM)
    }

    val dynamicColor: Flow<Boolean> = context.themeStore.data.map { prefs ->
        prefs[keyDynamicColor] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeStore.edit { it[keyThemeMode] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.themeStore.edit { it[keyDynamicColor] = enabled }
    }
}
