package com.gymsesh.app.ui.screens.stats

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymsesh.app.R
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.ui.theme.GymSeshTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Stats / Insight screen: a windowed muscle-group donut with legend, personal records
 * and recent workout history. Scrollable; state comes from [StatsViewModel].
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StatsContent(
        state = state,
        onWindowChange = viewModel::setWindow,
        modifier = modifier,
    )
}

/** Stateless body — hoisted so previews can render it with sample state. */
@Composable
private fun StatsContent(
    state: StatsUiState,
    onWindowChange: (StatsWindow) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.screen_stats_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(12.dp))

        StreakBanner(streak = state.currentStreak)

        Spacer(Modifier.height(16.dp))

        SectionHeader(stringResource(R.string.stats_section_calendar))
        GymCalendar(workoutDays = state.workoutDays)

        Spacer(Modifier.height(24.dp))

        WindowSelector(selected = state.window, onWindowChange = onWindowChange)

        Spacer(Modifier.height(16.dp))

        SectionHeader(stringResource(R.string.stats_section_balance))
        MuscleGroupPieChart(slices = state.muscleSlices)

        Spacer(Modifier.height(24.dp))

        SectionHeader(stringResource(R.string.stats_section_prs))
        if (state.prs.isEmpty()) {
            EmptyHint(stringResource(R.string.stats_prs_empty))
        } else {
            state.prs.forEach { pr -> PrRow(pr) }
        }

        Spacer(Modifier.height(24.dp))

        SectionHeader(stringResource(R.string.stats_section_history))
        if (state.sessions.isEmpty()) {
            EmptyHint(stringResource(R.string.stats_history_empty))
        } else {
            state.sessions.forEach { session -> HistoryRow(session) }
        }
    }
}

@Composable
private fun WindowSelector(
    selected: StatsWindow,
    onWindowChange: (StatsWindow) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatsWindow.entries.forEach { window ->
            FilterChip(
                selected = window == selected,
                onClick = { onWindowChange(window) },
                label = { Text(stringResource(window.labelRes())) },
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun PrRow(pr: PrEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Text(
            text = pr.exerciseName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(
                R.string.stats_pr_detail,
                formatKg(pr.weightKg),
                pr.reps,
                formatKg(pr.est1RM),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StreakBanner(streak: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = if (streak > 0) {
                stringResource(R.string.stats_streak, streak)
            } else {
                stringResource(R.string.stats_streak_zero)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
        )
    }
}

/**
 * A tappable history row. Collapsed it shows the session date + summary; tapping toggles
 * an in-place expansion listing each exercise and its logged sets ([SessionSummary.exercises]).
 */
@Composable
private fun HistoryRow(session: SessionSummary) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(session.dateEpochDay),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.stats_history_summary,
                        session.exerciseCount,
                        session.totalSets,
                        formatVolume(session.totalVolumeKg),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = stringResource(
                    if (expanded) R.string.stats_history_collapse else R.string.stats_history_expand,
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (expanded) {
            Spacer(Modifier.height(4.dp))
            session.exercises.forEach { detail -> ExerciseDetailBlock(detail) }
        }
    }
}

@Composable
private fun ExerciseDetailBlock(detail: ExerciseDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 6.dp),
    ) {
        Text(
            text = detail.exerciseName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        detail.sets.forEach { set ->
            val line = stringResource(R.string.stats_set_line, set.reps, formatKg(set.weightKg))
            Text(
                text = if (set.isWarmup) {
                    "$line · ${stringResource(R.string.stats_set_warmup)}"
                } else {
                    line
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    )
}

private fun StatsWindow.labelRes(): Int = when (this) {
    StatsWindow.THIS_WEEK -> R.string.stats_window_this_week
    StatsWindow.THIS_MONTH -> R.string.stats_window_this_month
    StatsWindow.ALL_TIME -> R.string.stats_window_all_time
}

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())

private fun formatDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(dateFormatter)

/** Whole-number kg with thousands grouping (e.g. `4200.0` -> "4,200"). */
private fun formatVolume(kg: Double): String =
    NumberFormat.getIntegerInstance(Locale.getDefault()).format(Math.round(kg))

/** Trims a trailing `.0` (e.g. `100.0` -> "100", `102.5` -> "102.5"). */
private fun formatKg(kg: Double): String =
    if (kg % 1.0 == 0.0) kg.toInt().toString() else String.format(Locale.US, "%.1f", kg)

// --- Previews ---------------------------------------------------------------

private val previewSlices: List<MuscleSlice> = listOf(
    MuscleSlice(MuscleGroup.CHEST, 12.0, 0.30f),
    MuscleSlice(MuscleGroup.BACK, 9.0, 0.225f),
    MuscleSlice(MuscleGroup.QUADS, 7.5, 0.1875f),
    MuscleSlice(MuscleGroup.SHOULDERS, 5.0, 0.125f),
    MuscleSlice(MuscleGroup.BICEPS, 3.5, 0.0875f),
    MuscleSlice(MuscleGroup.ABS, 3.0, 0.075f),
)

private val previewToday: LocalDate = LocalDate.now()

/** A few recent gym days (incl. today) so the calendar highlights and the streak read > 0. */
private val previewWorkoutDays: Set<Long> = setOf(
    previewToday.toEpochDay(),
    previewToday.minusDays(1).toEpochDay(),
    previewToday.minusDays(2).toEpochDay(),
    previewToday.minusDays(5).toEpochDay(),
)

private val previewState = StatsUiState(
    window = StatsWindow.THIS_WEEK,
    muscleSlices = previewSlices,
    workoutDays = previewWorkoutDays,
    currentStreak = 3,
    prs = listOf(
        PrEntry("Barbell Bench Press", 100.0, 5, 116.7),
        PrEntry("Back Squat", 140.0, 3, 154.0),
        PrEntry("Deadlift", 180.0, 1, 186.0),
    ),
    sessions = listOf(
        SessionSummary(
            sessionId = 3,
            dateEpochDay = previewToday.toEpochDay(),
            exerciseCount = 2,
            totalSets = 5,
            totalVolumeKg = 4200.0,
            exercises = listOf(
                ExerciseDetail(
                    exerciseName = "Barbell Bench Press",
                    sets = listOf(
                        SetLine(1, 12, 40.0, isWarmup = true),
                        SetLine(2, 8, 80.0, isWarmup = false),
                        SetLine(3, 8, 80.0, isWarmup = false),
                    ),
                ),
                ExerciseDetail(
                    exerciseName = "Incline Dumbbell Press",
                    sets = listOf(
                        SetLine(1, 10, 30.0, isWarmup = false),
                        SetLine(2, 10, 30.0, isWarmup = false),
                    ),
                ),
            ),
        ),
        SessionSummary(2, 20328, exerciseCount = 4, totalSets = 16, totalVolumeKg = 5100.0),
        SessionSummary(1, 20325, exerciseCount = 2, totalSets = 8, totalVolumeKg = 2600.0),
    ),
)

@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    GymSeshTheme {
        StatsContent(state = previewState, onWindowChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenEmptyPreview() {
    GymSeshTheme {
        StatsContent(state = StatsUiState(), onWindowChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MuscleGroupPieChartPreview() {
    GymSeshTheme {
        MuscleGroupPieChart(slices = previewSlices)
    }
}
