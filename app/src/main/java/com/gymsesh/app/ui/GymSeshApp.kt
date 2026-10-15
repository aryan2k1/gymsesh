package com.gymsesh.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.gymsesh.app.ui.navigation.GymSeshBottomBar
import com.gymsesh.app.ui.navigation.GymSeshNavHost
import com.gymsesh.app.ui.theme.GymSeshTheme
import com.gymsesh.app.ui.theme.GymSeshThemeRoot

/**
 * Root composable for the single-Activity app. Wraps the app scaffold in
 * [GymSeshThemeRoot] so the user's persisted theme (palette / accent / dynamic
 * colour / dark mode) is applied at runtime.
 *
 * Named `GymSeshRoot` to avoid clashing with the `GymSeshApp` Application class.
 */
@Composable
fun GymSeshRoot() {
    GymSeshThemeRoot {
        GymSeshScaffold()
    }
}

@Composable
private fun GymSeshScaffold() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { GymSeshBottomBar(navController = navController) },
    ) { innerPadding ->
        GymSeshNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GymSeshRootPreview() {
    // Preview uses the static theme (no Hilt) to render the default pink palette.
    GymSeshTheme {
        GymSeshScaffold()
    }
}
