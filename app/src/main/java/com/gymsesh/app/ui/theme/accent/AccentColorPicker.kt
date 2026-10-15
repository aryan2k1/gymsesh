package com.gymsesh.app.ui.theme.accent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gymsesh.app.ui.theme.GymSeshTheme

/**
 * Preset swatch grid for choosing a custom accent colour. Selecting a swatch
 * emits its ARGB value as a [Long]; selecting the "default" chip emits `null`
 * to clear the accent and fall back to the palette. Hosted later by the Settings
 * screen.
 */
val ACCENT_PRESETS: List<Long> = listOf(
    0xFFC2185B, // pink
    0xFFE91E63, // rose
    0xFF9C27B0, // purple
    0xFF6750A4, // lavender
    0xFF3F51B5, // indigo
    0xFF2196F3, // blue
    0xFF009688, // teal
    0xFF006A60, // mint
    0xFF4CAF50, // green
    0xFFFF9800, // orange
    0xFFFF5722, // deep orange
    0xFF795548, // brown
)

@Composable
fun AccentColorPicker(
    selectedArgb: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Accent colour",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        // "Default" chip clears the custom accent.
        DefaultAccentChip(
            selected = selectedArgb == null,
            onClick = { onSelect(null) },
        )
        ACCENT_PRESETS.chunked(columns).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowColors.forEach { argb ->
                    AccentSwatch(
                        argb = argb,
                        selected = selectedArgb == argb,
                        onClick = { onSelect(argb) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    argb: Long,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = Color(argb.toInt())
    val check = if (color.luminance() > 0.5f) Color.Black else Color.White
    val borderColor =
        if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(width = if (selected) 3.dp else 1.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = check,
            )
        }
    }
}

@Composable
private fun DefaultAccentChip(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = "Use palette default",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccentColorPickerPreview() {
    GymSeshTheme {
        AccentColorPicker(
            selectedArgb = 0xFF6750A4,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccentColorPickerDefaultSelectedPreview() {
    GymSeshTheme {
        AccentColorPicker(
            selectedArgb = null,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
