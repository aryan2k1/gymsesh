package com.gymsesh.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsesh.app.data.prefs.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Exposes the persisted [ThemeSettings] as observable state and offers setters
 * that write straight back to DataStore. Used by [GymSeshThemeRoot] to drive the
 * theme, and later by the Settings screen to mutate it.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val prefs: ThemePreferences,
) : ViewModel() {

    val settings: StateFlow<ThemeSettings> = prefs.themeSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeSettings(),
    )

    fun setPalette(palette: AppPalette) = viewModelScope.launch {
        prefs.setPalette(palette)
    }

    fun setCustomAccent(argb: Long?) = viewModelScope.launch {
        prefs.setCustomAccent(argb)
    }

    fun setUseDynamicColor(enabled: Boolean) = viewModelScope.launch {
        prefs.setUseDynamicColor(enabled)
    }

    fun setDarkMode(mode: DarkMode) = viewModelScope.launch {
        prefs.setDarkMode(mode)
    }
}
