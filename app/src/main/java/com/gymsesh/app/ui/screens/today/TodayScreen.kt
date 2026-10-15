package com.gymsesh.app.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymsesh.app.R
import com.gymsesh.app.ui.theme.GymSeshTheme

/**
 * Today tab (W3-SPLITS). Shows the session derived from the active split's
 * current rotation day, or an empty state prompting the user to pick a split.
 * Stateless rendering lives in [TodayScreenContent] so previews can drive it.
 */
@Composable
fun TodayScreen(
    onStartWorkout: () -> Unit,
    onManageSplits: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TodayScreenContent(
        state = state,
        onStartWorkout = {
            // Record today's split day for the Log tab to pre-fill, then navigate.
            viewModel.prepareStart()
            onStartWorkout()
        },
        onManageSplits = onManageSplits,
        onSkipDay = viewModel::advanceDay,
        modifier = modifier,
    )
}

@Composable
private fun TodayScreenContent(
    state: TodayUiState,
    onStartWorkout: () -> Unit,
    onManageSplits: () -> Unit,
    onSkipDay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.hasActiveSplit) {
        TodayEmptyState(
            streak = state.streak,
            onManageSplits = onManageSplits,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.splitName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            TextButton(onClick = onManageSplits) {
                Text(stringResource(R.string.today_change_split))
            }
        }
        Text(
            text = state.dayName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.today_day_position, state.dayPosition, state.dayCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        StreakBadge(streak = state.streak)

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.exercises) { exercise ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.today_exercise_target,
                                exercise.targetSets,
                                exercise.targetReps,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.today_start_workout))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onSkipDay,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.today_skip_day))
        }
    }
}

@Composable
private fun TodayEmptyState(
    streak: Int,
    onManageSplits: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.today_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.today_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        StreakBadge(streak = streak)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onManageSplits) {
            Text(stringResource(R.string.today_choose_split))
        }
    }
}

/**
 * A small "🔥 N-day streak" badge (or a "start your streak" prompt at 0).
 * Uses theme colours; pluralised via string resources.
 */
@Composable
private fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
) {
    val text = if (streak <= 0) {
        stringResource(R.string.today_streak_none)
    } else {
        pluralStringResource(R.plurals.today_streak, streak, streak)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenActivePreview() {
    GymSeshTheme {
        TodayScreenContent(
            state = TodayUiState(
                hasActiveSplit = true,
                splitName = "Push / Pull / Legs",
                dayName = "Push",
                dayCount = 3,
                dayPosition = 1,
                exercises = listOf(
                    TodayExercise("Barbell Bench Press", 4, 6),
                    TodayExercise("Incline Dumbbell Bench Press", 3, 10),
                    TodayExercise("Dumbbell Lateral Raise", 3, 15),
                    TodayExercise("Rope Triceps Pushdown", 3, 12),
                ),
                streak = 5,
            ),
            onStartWorkout = {},
            onManageSplits = {},
            onSkipDay = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenEmptyPreview() {
    GymSeshTheme {
        TodayScreenContent(
            state = TodayUiState(hasActiveSplit = false, streak = 1),
            onStartWorkout = {},
            onManageSplits = {},
            onSkipDay = {},
        )
    }
}
