package com.gymsesh.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.gymsesh.app.R

/**
 * The five top-level destinations reachable from the bottom navigation bar.
 *
 * Each entry carries its stable [route] string (used by the [NavHost]), a
 * [labelRes] for its bottom-bar label, and a Material [icon].
 */
enum class GymSeshDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    TODAY(
        route = "today",
        labelRes = R.string.nav_today,
        icon = Icons.Filled.Today,
    ),
    LIBRARY(
        route = "library",
        labelRes = R.string.nav_library,
        icon = Icons.Filled.FitnessCenter,
    ),
    LOG(
        route = "log",
        labelRes = R.string.nav_log,
        icon = Icons.Filled.EditNote,
    ),
    STATS(
        route = "stats",
        labelRes = R.string.nav_stats,
        icon = Icons.Filled.BarChart,
    ),
    SETTINGS(
        route = "settings",
        labelRes = R.string.nav_settings,
        icon = Icons.Filled.Settings,
    );

    companion object {
        /** The destination shown on first launch. */
        val START: GymSeshDestination = TODAY
    }
}
