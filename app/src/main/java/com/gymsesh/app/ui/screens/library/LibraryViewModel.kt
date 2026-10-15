package com.gymsesh.app.ui.screens.library

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Immutable UI state for the exercise library.
 *
 * [filtered] is the derived list the list renders: [all] narrowed by [query]
 * (case-insensitive name match) plus the optional [muscleFilter] / [equipmentFilter].
 */
@Immutable
data class LibraryUiState(
    val all: List<Exercise> = emptyList(),
    val query: String = "",
    val muscleFilter: MuscleGroup? = null,
    val equipmentFilter: Equipment? = null,
    val filtered: List<Exercise> = emptyList(),
)

/**
 * Backs [LibraryScreen]. Observes the exercise catalogue as a [kotlinx.coroutines.flow.Flow]
 * and combines it with the live search query and filter selections into a single
 * immutable [LibraryUiState] exposed as a [StateFlow].
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    repository: ExerciseRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val muscleFilter = MutableStateFlow<MuscleGroup?>(null)
    private val equipmentFilter = MutableStateFlow<Equipment?>(null)

    val state: StateFlow<LibraryUiState> = combine(
        repository.observeExercises(),
        query,
        muscleFilter,
        equipmentFilter,
    ) { all, q, muscle, equipment ->
        LibraryUiState(
            all = all,
            query = q,
            muscleFilter = muscle,
            equipmentFilter = equipment,
            filtered = filter(all, q, muscle, equipment),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    /** Toggles the muscle filter: selecting the active one clears it. */
    fun setMuscleFilter(value: MuscleGroup?) {
        muscleFilter.value = if (muscleFilter.value == value) null else value
    }

    /** Toggles the equipment filter: selecting the active one clears it. */
    fun setEquipmentFilter(value: Equipment?) {
        equipmentFilter.value = if (equipmentFilter.value == value) null else value
    }

    private companion object {
        fun filter(
            all: List<Exercise>,
            query: String,
            muscle: MuscleGroup?,
            equipment: Equipment?,
        ): List<Exercise> {
            val trimmed = query.trim()
            return all.filter { exercise ->
                (trimmed.isEmpty() || exercise.name.contains(trimmed, ignoreCase = true)) &&
                    (muscle == null || exercise.primaryMuscle == muscle) &&
                    (equipment == null || exercise.equipment == equipment)
            }
        }
    }
}
