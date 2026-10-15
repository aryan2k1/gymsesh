package com.gymsesh.app.ui.screens.splits

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymsesh.app.R
import com.gymsesh.app.data.model.SplitType
import com.gymsesh.app.ui.theme.GymSeshTheme

/**
 * Splits tab (W3-SPLITS). Lists prebuilt (and cloned CUSTOM) split templates.
 * Tapping a card expands it to show each day and its exercises. Each card offers
 * "Set as active" (drives the Today tab) and "Clone" (into a CUSTOM copy).
 * Stateless rendering is in [SplitsScreenContent] for previews.
 */
@Composable
fun SplitsScreen(
    viewModel: SplitsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SplitsScreenContent(
        state = state,
        onSetActive = viewModel::selectActive,
        onClone = viewModel::clone,
        modifier = modifier,
    )
}

@Composable
private fun SplitsScreenContent(
    state: SplitsUiState,
    onSetActive: (Long) -> Unit,
    onClone: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.templates.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.splits_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splits_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    var expandedId by rememberSaveable { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.templates, key = { it.id }) { template ->
            SplitTemplateCard(
                template = template,
                isActive = template.id == state.activeTemplateId,
                isExpanded = template.id == expandedId,
                onToggle = { expandedId = if (expandedId == template.id) null else template.id },
                onSetActive = { onSetActive(template.id) },
                onClone = { onClone(template.id) },
            )
        }
    }
}

@Composable
private fun SplitTemplateCard(
    template: SplitCard,
    isActive: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onSetActive: () -> Unit,
    onClone: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.splits_day_count, template.days.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isActive) {
                    AssistChip(
                        onClick = onToggle,
                        label = { Text(stringResource(R.string.splits_active_badge)) },
                    )
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                template.days.forEach { day ->
                    Text(
                        text = day.dayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    day.exercises.forEach { exercise ->
                        Text(
                            text = stringResource(
                                R.string.splits_exercise_target,
                                exercise.name,
                                exercise.targetSets,
                                exercise.targetReps,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onSetActive, enabled = !isActive) {
                    Text(stringResource(R.string.splits_set_active))
                }
                OutlinedButton(onClick = onClone) {
                    Text(stringResource(R.string.splits_clone))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplitsScreenPreview() {
    GymSeshTheme {
        SplitsScreenContent(
            state = SplitsUiState(
                activeTemplateId = 1L,
                templates = listOf(
                    SplitCard(
                        id = 1L,
                        name = "Push / Pull / Legs",
                        type = SplitType.PPL,
                        days = listOf(
                            SplitDayCard(
                                dayName = "Push",
                                exercises = listOf(
                                    SplitExerciseLine("Barbell Bench Press", 4, 6),
                                    SplitExerciseLine("Overhead Press", 3, 8),
                                ),
                            ),
                            SplitDayCard(
                                dayName = "Pull",
                                exercises = listOf(
                                    SplitExerciseLine("Pull-Up", 3, 8),
                                ),
                            ),
                        ),
                    ),
                    SplitCard(
                        id = 2L,
                        name = "Upper / Lower",
                        type = SplitType.UPPER_LOWER,
                        days = listOf(
                            SplitDayCard(dayName = "Upper", exercises = emptyList()),
                            SplitDayCard(dayName = "Lower", exercises = emptyList()),
                        ),
                    ),
                ),
            ),
            onSetActive = {},
            onClone = {},
        )
    }
}
