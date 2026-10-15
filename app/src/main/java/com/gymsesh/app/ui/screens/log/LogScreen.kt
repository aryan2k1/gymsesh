package com.gymsesh.app.ui.screens.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import com.gymsesh.app.R
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.ui.theme.GymSeshTheme

/**
 * Workout logging tab (BUILD_BIBLE §4). Self-contained: an empty "start" state,
 * or an active session of exercises → editable sets, plus a rest timer. The
 * exercise picker is a [ModalBottomSheet]. All state comes from [LogViewModel];
 * the rendering lives in the stateless [LogScreenContent] so previews can drive
 * it with fake state.
 */
@Composable
fun LogScreen(
    viewModel: LogViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    // Pre-fill from a "Start this workout" request made on the Today tab, if any.
    LaunchedEffect(Unit) {
        viewModel.consumePendingStart()
    }

    LogScreenContent(
        uiState = uiState,
        exercises = exercises,
        callbacks = LogCallbacks(
            onStart = viewModel::startWorkout,
            onFinish = viewModel::finishWorkout,
            onOpenPicker = viewModel::openPicker,
            onClosePicker = viewModel::closePicker,
            onQueryChange = viewModel::setPickerQuery,
            onPickExercise = viewModel::addExercise,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onRemoveSet = viewModel::removeSet,
            onRepsChange = viewModel::updateReps,
            onWeightChange = viewModel::updateWeight,
            onRpeChange = viewModel::updateRpe,
            onToggleDone = viewModel::toggleSetDone,
            onToggleWarmup = viewModel::toggleWarmup,
        ),
        modifier = modifier,
    )
}

/** Grouped callbacks so the stateless content keeps a readable signature. */
class LogCallbacks(
    val onStart: () -> Unit = {},
    val onFinish: () -> Unit = {},
    val onOpenPicker: () -> Unit = {},
    val onClosePicker: () -> Unit = {},
    val onQueryChange: (String) -> Unit = {},
    val onPickExercise: (Exercise) -> Unit = {},
    val onRemoveExercise: (Long) -> Unit = {},
    val onAddSet: (Long) -> Unit = {},
    val onRemoveSet: (Long, Long) -> Unit = { _, _ -> },
    val onRepsChange: (Long, Long, String) -> Unit = { _, _, _ -> },
    val onWeightChange: (Long, Long, String) -> Unit = { _, _, _ -> },
    val onRpeChange: (Long, Long, String) -> Unit = { _, _, _ -> },
    val onToggleDone: (Long, Long, Boolean) -> Unit = { _, _, _ -> },
    val onToggleWarmup: (Long, Long, Boolean) -> Unit = { _, _, _ -> },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreenContent(
    uiState: LogUiState,
    exercises: List<Exercise>,
    callbacks: LogCallbacks,
    modifier: Modifier = Modifier,
) {
    val session = uiState.session

    Box(modifier = modifier.fillMaxSize()) {
        if (session == null) {
            EmptyState(onStart = callbacks.onStart, modifier = Modifier.fillMaxSize())
        } else {
            ActiveSessionContent(session = session, callbacks = callbacks)
        }

        if (uiState.isPickerOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = callbacks.onClosePicker,
                sheetState = sheetState,
            ) {
                ExercisePickerSheet(
                    query = uiState.pickerQuery,
                    exercises = exercises,
                    onQueryChange = callbacks.onQueryChange,
                    onPick = callbacks.onPickExercise,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.log_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.log_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Button(onClick = onStart) {
            Text(stringResource(R.string.log_start_workout))
        }
    }
}

@Composable
private fun ActiveSessionContent(
    session: ActiveSession,
    callbacks: LogCallbacks,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Pinned above the scroll area so its ticking state survives scrolling
        // (it is only disposed — and thus paused — when the Log tab is left).
        RestTimer(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(session.exercises, key = { it.uid }) { entry ->
                ExerciseCard(entry = entry, callbacks = callbacks)
            }

            item(key = "add_exercise") {
                OutlinedButton(
                    onClick = callbacks.onOpenPicker,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(
                        text = stringResource(R.string.log_add_exercise),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            item(key = "finish") {
                Button(
                    onClick = callbacks.onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = session.exercises.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.log_finish_workout))
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    entry: ExerciseEntry,
    callbacks: LogCallbacks,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { callbacks.onRemoveExercise(entry.uid) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.log_remove_exercise),
                    )
                }
            }

            entry.sets.forEachIndexed { index, set ->
                SetRow(
                    exerciseUid = entry.uid,
                    setNumber = index + 1,
                    set = set,
                    callbacks = callbacks,
                )
            }

            TextButton(
                onClick = { callbacks.onAddSet(entry.uid) },
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = stringResource(R.string.log_add_set),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    exerciseUid: Long,
    setNumber: Int,
    set: SetEntry,
    callbacks: LogCallbacks,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.log_set_number, setNumber),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = set.isWarmup,
                    onClick = { callbacks.onToggleWarmup(exerciseUid, set.uid, !set.isWarmup) },
                    label = { Text(stringResource(R.string.log_warmup)) },
                )
                IconButton(onClick = { callbacks.onRemoveSet(exerciseUid, set.uid) }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.log_remove_set),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NumberField(
                value = set.reps,
                label = stringResource(R.string.log_reps),
                onValueChange = { callbacks.onRepsChange(exerciseUid, set.uid, it) },
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = set.weightKg,
                label = stringResource(R.string.log_weight),
                onValueChange = { callbacks.onWeightChange(exerciseUid, set.uid, it) },
                modifier = Modifier.weight(1f),
                decimal = true,
            )
            NumberField(
                value = set.rpe,
                label = stringResource(R.string.log_rpe),
                onValueChange = { callbacks.onRpeChange(exerciseUid, set.uid, it) },
                modifier = Modifier.weight(1f),
                decimal = true,
            )
            Checkbox(
                checked = set.isDone,
                onCheckedChange = { callbacks.onToggleDone(exerciseUid, set.uid, it) },
            )
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    query: String,
    exercises: List<Exercise>,
    onQueryChange: (String) -> Unit,
    onPick: (Exercise) -> Unit,
) {
    val filtered = if (query.isBlank()) {
        exercises
    } else {
        exercises.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.log_pick_exercise_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.log_search_exercises)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
        if (filtered.isEmpty()) {
            Text(
                text = stringResource(R.string.log_no_exercises),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filtered, key = { it.id }) { exercise ->
                    ListItem(
                        headlineContent = { Text(exercise.name) },
                        supportingContent = {
                            Text(exercise.primaryMuscle.name.lowercase())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(exercise) },
                        trailingContent = {
                            IconButton(onClick = { onPick(exercise) }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = stringResource(R.string.log_add_exercise),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

// --- Previews ---------------------------------------------------------------

private fun sampleExercise(id: Long, name: String, muscle: MuscleGroup) = Exercise(
    id = id,
    name = name,
    primaryMuscle = muscle,
    equipment = Equipment.BARBELL,
)

private val sampleExercises = listOf(
    sampleExercise(1, "Barbell Bench Press", MuscleGroup.CHEST),
    sampleExercise(2, "Back Squat", MuscleGroup.QUADS),
    sampleExercise(3, "Deadlift", MuscleGroup.BACK),
)

private val sampleActiveSession = ActiveSession(
    dateEpochDay = 20_000L,
    exercises = listOf(
        ExerciseEntry(
            uid = 1,
            exerciseId = 1,
            name = "Barbell Bench Press",
            sets = listOf(
                SetEntry(uid = 1, reps = "10", weightKg = "40", isWarmup = true, isDone = true),
                SetEntry(uid = 2, reps = "8", weightKg = "60", rpe = "8", isDone = true),
                SetEntry(uid = 3, reps = "8", weightKg = "60"),
            ),
        ),
        ExerciseEntry(
            uid = 2,
            exerciseId = 3,
            name = "Deadlift",
            sets = listOf(SetEntry(uid = 4, reps = "5", weightKg = "100")),
        ),
    ),
)

@Preview(showBackground = true, name = "Log — empty state")
@Composable
private fun LogScreenEmptyPreview() {
    GymSeshTheme {
        LogScreenContent(
            uiState = LogUiState(),
            exercises = sampleExercises,
            callbacks = LogCallbacks(),
        )
    }
}

@Preview(showBackground = true, name = "Log — active session")
@Composable
private fun LogScreenActivePreview() {
    GymSeshTheme {
        LogScreenContent(
            uiState = LogUiState(session = sampleActiveSession),
            exercises = sampleExercises,
            callbacks = LogCallbacks(),
        )
    }
}
