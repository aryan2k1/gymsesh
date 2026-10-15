package com.gymsesh.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gymsesh.app.ui.theme.AppPalette
import com.gymsesh.app.ui.theme.DarkMode
import com.gymsesh.app.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed store for the user's [ThemeSettings]. Exposes a [Flow] of the
 * current settings plus suspend setters for each field. Provided as a singleton
 * via Hilt (see `di/PrefsModule.kt`).
 */
class ThemePreferences(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val PALETTE = stringPreferencesKey("theme_palette")
        val ACCENT = longPreferencesKey("theme_custom_accent")
        val DYNAMIC = booleanPreferencesKey("theme_use_dynamic_color")
        val DARK_MODE = stringPreferencesKey("theme_dark_mode")
    }

    // A negative sentinel marks "no custom accent" since Preferences has no
    // nullable Long key.
    private val noAccent = Long.MIN_VALUE

    /** Current theme settings, emitting on every change. */
    val themeSettings: Flow<ThemeSettings> = dataStore.data.map { prefs ->
        val accent = prefs[Keys.ACCENT] ?: noAccent
        ThemeSettings(
            palette = AppPalette.fromName(prefs[Keys.PALETTE]),
            customAccentArgb = if (accent == noAccent) null else accent,
            useDynamicColor = prefs[Keys.DYNAMIC] ?: false,
            darkMode = DarkMode.fromName(prefs[Keys.DARK_MODE]),
        )
    }

    suspend fun setPalette(palette: AppPalette) {
        dataStore.edit { it[Keys.PALETTE] = palette.name }
    }

    /** Pass `null` to clear the custom accent and fall back to the palette. */
    suspend fun setCustomAccent(argb: Long?) {
        dataStore.edit {
            if (argb == null) it.remove(Keys.ACCENT) else it[Keys.ACCENT] = argb
        }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC] = enabled }
    }

    suspend fun setDarkMode(mode: DarkMode) {
        dataStore.edit { it[Keys.DARK_MODE] = mode.name }
    }
}
