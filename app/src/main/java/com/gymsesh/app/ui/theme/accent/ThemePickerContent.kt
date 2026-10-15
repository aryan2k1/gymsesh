package com.gymsesh.app.ui.theme.accent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gymsesh.app.ui.theme.AppPalette
import com.gymsesh.app.ui.theme.DarkMode
import com.gymsesh.app.ui.theme.GymSeshTheme
import com.gymsesh.app.ui.theme.ThemeSettings

/**
 * Stateless theme-picker surface: palette swatches, custom accent picker,
 * a Material You (dynamic colour) toggle, and a light/dark-mode selector.
 * Reads from a [ThemeSettings] and reports every change via callbacks; a
 * Settings screen supplies the state and persists it.
 */
@Composable
fun ThemePickerContent(
    settings: ThemeSettings,
    onPaletteSelected: (AppPalette) -> Unit,
    onAccentSelected: (Long?) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onDarkModeSelected: (DarkMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // --- Palettes -------------------------------------------------------
        Text(
            text = "Palette",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppPalette.entries.forEach { palette ->
                PaletteRow(
                    palette = palette,
                    selected = settings.palette == palette && !settings.useDynamicColor,
                    onClick = { onPaletteSelected(palette) },
                )
            }
        }

        HorizontalDivider()

        // --- Custom accent --------------------------------------------------
        AccentColorPicker(
            selectedArgb = settings.customAccentArgb,
            onSelect = onAccentSelected,
        )

        HorizontalDivider()

        // --- Dynamic colour -------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Material You",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Use dynamic colour from your wallpaper (Android 12+)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = settings.useDynamicColor,
                onCheckedChange = onDynamicColorChanged,
            )
        }

        HorizontalDivider()

        // --- Dark mode ------------------------------------------------------
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(modifier = Modifier.selectableGroup()) {
            DarkMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = settings.darkMode == mode,
                            onClick = { onDarkModeSelected(mode) },
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = settings.darkMode == mode,
                        onClick = null,
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteRow(
    palette: AppPalette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SwatchTrio(palette)
        Text(
            text = palette.displayName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SwatchTrio(palette: AppPalette) {
    val scheme = palette.light
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Dot(scheme.primary)
        Dot(scheme.secondary)
        Dot(scheme.primaryContainer)
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = CircleShape),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ThemePickerContentPreview() {
    GymSeshTheme {
        ThemePickerContent(
            settings = ThemeSettings(palette = AppPalette.MINT),
            onPaletteSelected = {},
            onAccentSelected = {},
            onDynamicColorChanged = {},
            onDarkModeSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
