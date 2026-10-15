package com.gymsesh.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * GymSesh theme.
 *
 * Colour resolution order (see [resolveColorScheme]):
 *  1. **Base scheme** — Material You dynamic colour ([dynamicLightColorScheme] /
 *     [dynamicDarkColorScheme]) when [ThemeSettings.useDynamicColor] is on *and*
 *     the device is API 31+; otherwise the selected [AppPalette].
 *  2. **Custom accent** — when [ThemeSettings.customAccentArgb] is set it
 *     overrides the base scheme's `primary` (with a contrast-matched `onPrimary`).
 *
 * Light vs dark is chosen from [ThemeSettings.darkMode] (SYSTEM follows the OS).
 *
 * The zero-argument overload defaults to a pink [ThemeSettings], so existing
 * callers and `@Preview`s keep compiling with no wiring.
 */
@Composable
fun GymSeshTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val dark = when (settings.darkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }
    val colorScheme = resolveColorScheme(settings, dark)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GymSeshTypography,
        content = content,
    )
}

/**
 * Hilt-driven entry point: reads persisted [ThemeSettings] from [ThemeViewModel]
 * and applies them. Prefer this at the app root; [GymSeshTheme] stays available
 * for previews and callers that already hold a [ThemeSettings].
 */
@Composable
fun GymSeshThemeRoot(
    viewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    GymSeshTheme(settings = settings, content = content)
}

@Composable
private fun resolveColorScheme(
    settings: ThemeSettings,
    dark: Boolean,
): ColorScheme {
    val base = if (settings.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        settings.palette.scheme(dark)
    }

    val accent = settings.customAccentArgb ?: return base
    val accentColor = Color(accent.toInt())
    return base.copy(
        primary = accentColor,
        onPrimary = contrastOn(accentColor),
    )
}

/** Black or white, whichever reads better on [background]. */
private fun contrastOn(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White
