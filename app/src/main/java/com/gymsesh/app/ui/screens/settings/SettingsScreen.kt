package com.gymsesh.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymsesh.app.R
import com.gymsesh.app.ui.theme.AppPalette
import com.gymsesh.app.ui.theme.DarkMode
import com.gymsesh.app.ui.theme.GymSeshTheme
import com.gymsesh.app.ui.theme.ThemeSettings
import com.gymsesh.app.ui.theme.ThemeViewModel
import com.gymsesh.app.ui.theme.accent.ThemePickerContent

/** App version shown in the About footer. */
private const val APP_VERSION = "0.1.0"

/**
 * Settings screen. Hosts the theme picker and drives the app theme by writing
 * every change straight back through [ThemeViewModel] (which persists to
 * DataStore and re-emits via [ThemeViewModel.settings]).
 *
 * The [viewModel] is defaulted so the NavHost's no-arg `SettingsScreen()` call
 * keeps compiling.
 */
@Composable
fun SettingsScreen(
    viewModel: ThemeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsContent(
        settings = settings,
        onPaletteSelected = viewModel::setPalette,
        onAccentSelected = viewModel::setCustomAccent,
        onDynamicColorChanged = viewModel::setUseDynamicColor,
        onDarkModeSelected = viewModel::setDarkMode,
        modifier = modifier,
    )
}

/**
 * Stateless settings surface: a titled, scrollable host around the reusable
 * [ThemePickerContent] plus an About footer. Kept stateless so it can be
 * previewed without Hilt.
 */
@Composable
private fun SettingsContent(
    settings: ThemeSettings,
    onPaletteSelected: (AppPalette) -> Unit,
    onAccentSelected: (Long?) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onDarkModeSelected: (DarkMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.screen_settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = stringResource(R.string.settings_appearance_header),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        ThemePickerContent(
            settings = settings,
            onPaletteSelected = onPaletteSelected,
            onAccentSelected = onAccentSelected,
            onDynamicColorChanged = onDynamicColorChanged,
            onDarkModeSelected = onDarkModeSelected,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.settings_about_header),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(
                R.string.settings_about_line,
                stringResource(R.string.app_name),
                APP_VERSION,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun SettingsScreenPreview() {
    GymSeshTheme {
        SettingsContent(
            settings = ThemeSettings(palette = AppPalette.PINK),
            onPaletteSelected = {},
            onAccentSelected = {},
            onDynamicColorChanged = {},
            onDarkModeSelected = {},
        )
    }
}
