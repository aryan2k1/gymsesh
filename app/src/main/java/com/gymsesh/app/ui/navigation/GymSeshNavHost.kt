package com.gymsesh.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gymsesh.app.ui.screens.library.ExerciseDetailScreen
import com.gymsesh.app.ui.screens.library.LibraryScreen
import com.gymsesh.app.ui.screens.log.LogScreen
import com.gymsesh.app.ui.screens.settings.SettingsScreen
import com.gymsesh.app.ui.screens.splits.SplitsScreen
import com.gymsesh.app.ui.screens.stats.StatsScreen
import com.gymsesh.app.ui.screens.today.TodayScreen

/** Non-tab detail routes reached from within a tab. */
object DetailRoutes {
    const val EXERCISE_ARG = "exerciseId"
    const val EXERCISE_DETAIL = "exercise_detail"
    fun exerciseDetail(id: Long) = "$EXERCISE_DETAIL/$id"

    /** Split management, reached from the Today tab. */
    const val SPLITS = "splits"
}

/**
 * Wires each [GymSeshDestination] route to its top-level screen composable,
 * plus the non-tab exercise-detail route reached from the library.
 */
@Composable
fun GymSeshNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = GymSeshDestination.START.route,
        modifier = modifier,
    ) {
        composable(GymSeshDestination.TODAY.route) {
            TodayScreen(
                onStartWorkout = {
                    navController.navigate(GymSeshDestination.LOG.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onManageSplits = { navController.navigate(DetailRoutes.SPLITS) },
            )
        }
        composable(DetailRoutes.SPLITS) { SplitsScreen() }
        composable(GymSeshDestination.LIBRARY.route) {
            LibraryScreen(
                onExerciseClick = { id ->
                    navController.navigate(DetailRoutes.exerciseDetail(id))
                },
            )
        }
        composable(GymSeshDestination.LOG.route) { LogScreen() }
        composable(GymSeshDestination.STATS.route) { StatsScreen() }
        composable(GymSeshDestination.SETTINGS.route) { SettingsScreen() }

        composable(
            route = "${DetailRoutes.EXERCISE_DETAIL}/{${DetailRoutes.EXERCISE_ARG}}",
            arguments = listOf(
                navArgument(DetailRoutes.EXERCISE_ARG) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(DetailRoutes.EXERCISE_ARG) ?: 0L
            ExerciseDetailScreen(
                exerciseId = id,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
