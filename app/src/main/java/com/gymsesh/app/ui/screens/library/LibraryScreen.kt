package com.gymsesh.app.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymsesh.app.R
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.ui.theme.GymSeshTheme

/**
 * Exercise Library: a searchable, filterable catalogue. Tapping a row navigates to
 * the detail screen via [onExerciseClick].
 */
@Composable
fun LibraryScreen(
    onExerciseClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LibraryContent(
        state = state,
        onQueryChange = viewModel::setQuery,
        onMuscleFilterChange = viewModel::setMuscleFilter,
        onEquipmentFilterChange = viewModel::setEquipmentFilter,
        onExerciseClick = onExerciseClick,
        modifier = modifier,
    )
}

/** Stateless body — hoisted so previews can render it with sample state. */
@Composable
private fun LibraryContent(
    state: LibraryUiState,
    onQueryChange: (String) -> Unit,
    onMuscleFilterChange: (MuscleGroup?) -> Unit,
    onEquipmentFilterChange: (Equipment?) -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.library_search_clear),
                        )
                    }
                }
            },
            placeholder = { Text(stringResource(R.string.library_search_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        FilterChipRow(
            label = stringResource(R.string.library_filter_muscle),
            options = MuscleGroup.entries,
            selected = state.muscleFilter,
            optionLabel = { it.label() },
            onSelected = onMuscleFilterChange,
        )
        FilterChipRow(
            label = stringResource(R.string.library_filter_equipment),
            options = Equipment.entries,
            selected = state.equipmentFilter,
            optionLabel = { it.label() },
            onSelected = onEquipmentFilterChange,
        )

        Spacer(Modifier.height(8.dp))

        if (state.filtered.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(state.filtered, key = { it.id }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun <T> FilterChipRow(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { Text(optionLabel(option)) },
            )
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(
                R.string.library_row_summary,
                exercise.primaryMuscle.label(),
                exercise.equipment.label(),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.library_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.library_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val previewSampleExercises: List<Exercise> = listOf(
    Exercise(
        id = 1,
        name = "Barbell Bench Press",
        primaryMuscle = MuscleGroup.CHEST,
        secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
        equipment = Equipment.BARBELL,
    ),
    Exercise(
        id = 2,
        name = "Pull-Up",
        primaryMuscle = MuscleGroup.BACK,
        secondaryMuscles = listOf(MuscleGroup.BICEPS),
        equipment = Equipment.BODYWEIGHT,
    ),
    Exercise(
        id = 3,
        name = "Back Squat",
        primaryMuscle = MuscleGroup.QUADS,
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
        equipment = Equipment.BARBELL,
    ),
)

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    GymSeshTheme {
        LibraryContent(
            state = LibraryUiState(
                all = previewSampleExercises,
                query = "",
                muscleFilter = MuscleGroup.CHEST,
                equipmentFilter = null,
                filtered = previewSampleExercises,
            ),
            onQueryChange = {},
            onMuscleFilterChange = {},
            onEquipmentFilterChange = {},
            onExerciseClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryScreenEmptyPreview() {
    GymSeshTheme {
        LibraryContent(
            state = LibraryUiState(),
            onQueryChange = {},
            onMuscleFilterChange = {},
            onEquipmentFilterChange = {},
            onExerciseClick = {},
        )
    }
}
