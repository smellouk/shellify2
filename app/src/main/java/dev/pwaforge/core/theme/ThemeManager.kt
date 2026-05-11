package dev.pwaforge.core.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.pwaforge.domain.model.EngineType
import dev.pwaforge.domain.model.UserAgentMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore by preferencesDataStore(name = "pwa_theme")

class ThemeManager(private val context: Context) {

    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyDynamicColor = booleanPreferencesKey("dynamic_color")
    private val keyDefaultUa = stringPreferencesKey("default_ua")
    private val keyDefaultEngine = stringPreferencesKey("default_engine")

    val themeMode: Flow<ThemeMode> = context.themeStore.data.map { prefs ->
        runCatching { ThemeMode.valueOf(prefs[keyThemeMode] ?: "") }.getOrDefault(ThemeMode.SYSTEM)
    }

    val dynamicColor: Flow<Boolean> = context.themeStore.data.map { prefs ->
        prefs[keyDynamicColor] ?: true
    }

    val defaultUaMode: Flow<UserAgentMode> = context.themeStore.data.map { prefs ->
        runCatching { UserAgentMode.valueOf(prefs[keyDefaultUa] ?: "") }.getOrDefault(UserAgentMode.CHROME_MOBILE)
    }

    val defaultEngineType: Flow<EngineType> = context.themeStore.data.map { prefs ->
        runCatching { EngineType.valueOf(prefs[keyDefaultEngine] ?: "") }.getOrDefault(EngineType.SYSTEM_WEBVIEW)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeStore.edit { it[keyThemeMode] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.themeStore.edit { it[keyDynamicColor] = enabled }
    }

    suspend fun setDefaultUaMode(mode: UserAgentMode) {
        context.themeStore.edit { it[keyDefaultUa] = mode.name }
    }

    suspend fun setDefaultEngineType(engine: EngineType) {
        context.themeStore.edit { it[keyDefaultEngine] = engine.name }
    }
}
