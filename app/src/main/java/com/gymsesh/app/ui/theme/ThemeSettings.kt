package com.gymsesh.app.ui.theme

import androidx.compose.runtime.Immutable

/** How the app decides between light and dark colour schemes. */
enum class DarkMode(val displayName: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        val DEFAULT: DarkMode = SYSTEM

        fun fromName(name: String?): DarkMode =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/**
 * Immutable snapshot of the user's theme preferences. Backed by DataStore and
 * consumed by [GymSeshTheme].
 *
 * @param palette          the selected built-in palette.
 * @param customAccentArgb an optional ARGB colour (as a [Long]) that overrides
 *                         the palette's [androidx.compose.material3.ColorScheme.primary];
 *                         `null` means "use the palette's primary".
 * @param useDynamicColor  when true and the device supports it (API 31+), use
 *                         Material You dynamic colour derived from the wallpaper.
 * @param darkMode         light/dark selection ([DarkMode.SYSTEM] follows the OS).
 */
@Immutable
data class ThemeSettings(
    val palette: AppPalette = AppPalette.DEFAULT,
    val customAccentArgb: Long? = null,
    val useDynamicColor: Boolean = false,
    val darkMode: DarkMode = DarkMode.DEFAULT,
)
