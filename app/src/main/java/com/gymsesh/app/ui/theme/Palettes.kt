package com.gymsesh.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Built-in colour palettes for GymSesh. Each palette is a full Material 3
 * light + dark [ColorScheme] derived from a seed colour. [PINK] is the default
 * and reuses the original theme values (see [Color.kt]).
 *
 * Palettes are represented as an enum so they can be persisted by name via
 * DataStore and enumerated in the theme picker UI.
 */
enum class AppPalette(
    val displayName: String,
    val light: ColorScheme,
    val dark: ColorScheme,
) {
    PINK("Pink", PinkLightScheme, PinkDarkScheme),
    LAVENDER("Lavender", LavenderLightScheme, LavenderDarkScheme),
    MINT("Mint", MintLightScheme, MintDarkScheme),
    MONOCHROME("Monochrome", MonochromeLightScheme, MonochromeDarkScheme);

    /** The scheme for the requested brightness. */
    fun scheme(dark: Boolean): ColorScheme = if (dark) this.dark else this.light

    companion object {
        val DEFAULT: AppPalette = PINK

        /** Safe lookup by [name]; falls back to [DEFAULT] for unknown values. */
        fun fromName(name: String?): AppPalette =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

// ---------------------------------------------------------------------------
// PINK — default. Colour seeds live in Color.kt.
// ---------------------------------------------------------------------------
val PinkLightScheme: ColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = PinkOnPrimary,
    primaryContainer = PinkPrimaryContainer,
    onPrimaryContainer = PinkOnPrimaryContainer,
    secondary = PinkSecondary,
    onSecondary = PinkOnSecondary,
    secondaryContainer = PinkSecondaryContainer,
    onSecondaryContainer = PinkOnSecondaryContainer,
    background = PinkBackground,
    onBackground = PinkOnBackground,
    surface = PinkSurface,
    onSurface = PinkOnSurface,
    surfaceVariant = PinkSurfaceVariant,
    onSurfaceVariant = PinkOnSurfaceVariant,
)

val PinkDarkScheme: ColorScheme = darkColorScheme(
    primary = PinkPrimaryDark,
    onPrimary = PinkOnPrimaryDark,
    primaryContainer = PinkPrimaryContainerDark,
    onPrimaryContainer = PinkOnPrimaryContainerDark,
    secondary = PinkSecondaryDark,
    onSecondary = PinkOnSecondaryDark,
    secondaryContainer = PinkSecondaryContainerDark,
    onSecondaryContainer = PinkOnSecondaryContainerDark,
    background = PinkBackgroundDark,
    onBackground = PinkOnBackgroundDark,
    surface = PinkSurfaceDark,
    onSurface = PinkOnSurfaceDark,
    surfaceVariant = PinkSurfaceVariantDark,
    onSurfaceVariant = PinkOnSurfaceVariantDark,
)

// ---------------------------------------------------------------------------
// LAVENDER — purple seed (#6750A4).
// ---------------------------------------------------------------------------
val LavenderLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
)

val LavenderDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
)

// ---------------------------------------------------------------------------
// MINT — teal/green seed (#006A60).
// ---------------------------------------------------------------------------
val MintLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF74F8E5),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF06201C),
    background = Color(0xFFF4FBF8),
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFF4FBF8),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
)

val MintDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF53DBC9),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF74F8E5),
    secondary = Color(0xFFB1CCC6),
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCCE8E2),
    background = Color(0xFF0E1513),
    onBackground = Color(0xFFDDE4E1),
    surface = Color(0xFF0E1513),
    onSurface = Color(0xFFDDE4E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C4),
)

// ---------------------------------------------------------------------------
// MONOCHROME — neutral grey seed.
// ---------------------------------------------------------------------------
val MonochromeLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF424750),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7DBE4),
    onPrimaryContainer = Color(0xFF10131A),
    secondary = Color(0xFF5A5D63),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDEE1E8),
    onSecondaryContainer = Color(0xFF171B1F),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474E),
)

val MonochromeDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFC0C6D0),
    onPrimary = Color(0xFF2A2F36),
    primaryContainer = Color(0xFF40454D),
    onPrimaryContainer = Color(0xFFDCE2EC),
    secondary = Color(0xFFC2C6CE),
    onSecondary = Color(0xFF2C2F36),
    secondaryContainer = Color(0xFF43474E),
    onSecondaryContainer = Color(0xFFDEE1E8),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
)
