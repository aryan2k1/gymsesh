package com.gymsesh.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gymsesh.app.R
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.ui.theme.GymSeshTheme

/**
 * Detail view for a single [Exercise]: image (or placeholder), name, muscle and
 * equipment chips, and instructions. The [exerciseId] is handed to the ViewModel,
 * which observes that exercise from the repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(exerciseId) { viewModel.load(exerciseId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExerciseDetailContent(
        state = state,
        onBack = onBack,
        modifier = modifier,
    )
}

/** Stateless body — hoisted so previews can render it with sample state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetailContent(
    state: ExerciseDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(state.exercise?.name ?: stringResource(R.string.screen_library_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.exercise_detail_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val exercise = state.exercise
        if (exercise == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                if (!state.isLoading) {
                    Text(
                        text = stringResource(R.string.exercise_detail_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            ExerciseDetailBody(
                exercise = exercise,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            )
        }
    }
}

@Composable
private fun ExerciseDetailBody(
    exercise: Exercise,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ImageArea(imagePath = exercise.imagePath)

        Text(
            text = exercise.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Section(title = stringResource(R.string.exercise_detail_primary_muscle)) {
            ChipFlow(labels = listOf(exercise.primaryMuscle.label()))
        }

        if (exercise.secondaryMuscles.isNotEmpty()) {
            Section(title = stringResource(R.string.exercise_detail_secondary_muscles)) {
                ChipFlow(labels = exercise.secondaryMuscles.map(MuscleGroup::label))
            }
        }

        Section(title = stringResource(R.string.exercise_detail_equipment)) {
            ChipFlow(labels = listOf(exercise.equipment.label()))
        }

        Section(title = stringResource(R.string.exercise_detail_instructions)) {
            Text(
                text = exercise.instructions?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.exercise_detail_no_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ImageArea(
    imagePath: String?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    if (imagePath != null) {
        AsyncImage(
            model = imagePath,
            contentDescription = stringResource(R.string.exercise_detail_image_desc),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(shape),
        )
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = stringResource(R.string.exercise_detail_placeholder_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp),
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            AssistChip(onClick = {}, label = { Text(label) })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailScreenPreview() {
    GymSeshTheme {
        ExerciseDetailContent(
            state = ExerciseDetailUiState(
                exercise = Exercise(
                    id = 1,
                    name = "Barbell Bench Press",
                    primaryMuscle = MuscleGroup.CHEST,
                    secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                    equipment = Equipment.BARBELL,
                    instructions = "Lie back on a flat bench. Grip the bar slightly wider " +
                        "than shoulder width, lower it to mid-chest, then press up to " +
                        "full extension.",
                ),
                isLoading = false,
            ),
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailNotFoundPreview() {
    GymSeshTheme {
        ExerciseDetailContent(
            state = ExerciseDetailUiState(exercise = null, isLoading = false),
            onBack = {},
        )
    }
}
