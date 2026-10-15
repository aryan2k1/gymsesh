package com.gymsesh.app.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Material 3 [NavigationBar] for the five top-level destinations. Reads the
 * current back-stack entry to highlight the active tab; tapping a tab switches
 * to it using single-top launch with state save/restore so each tab keeps its
 * own scroll/nav state.
 */
@Composable
fun GymSeshBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(modifier = modifier) {
        GymSeshDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.route
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    // If we're on a non-tab detail route (e.g. Splits or Exercise
                    // detail pushed on top of a tab), pop it first so tapping a tab
                    // returns to that tab's root instead of restoreState resurrecting
                    // the detail screen (notably when tapping the start tab, Today).
                    val current = currentDestination?.route
                    val onDetailRoute = current != null &&
                        GymSeshDestination.entries.none { it.route == current }
                    if (onDetailRoute) {
                        navController.popBackStack()
                    }
                    navController.navigate(destination.route) {
                        // Pop up to the start destination to avoid building a
                        // large back stack as tabs are re-selected.
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.labelRes),
                    )
                },
                label = { Text(text = stringResource(destination.labelRes)) },
            )
        }
    }
}
