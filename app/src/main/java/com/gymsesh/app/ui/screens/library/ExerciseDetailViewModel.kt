package com.gymsesh.app.ui.screens.library

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Immutable UI state for [ExerciseDetailScreen]. */
@Immutable
data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = true,
)

/**
 * Backs [ExerciseDetailScreen]. The id is supplied via [load] (the screen calls it
 * with the nav argument) and the single exercise is observed from the repository's
 * [ExerciseRepository.observeExercise] flow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    repository: ExerciseRepository,
) : ViewModel() {

    private val exerciseId = MutableStateFlow<Long?>(null)

    val state: StateFlow<ExerciseDetailUiState> = exerciseId
        .filterNotNull()
        .flatMapLatest { id -> repository.observeExercise(id) }
        .map { exercise -> ExerciseDetailUiState(exercise = exercise, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseDetailUiState(),
        )

    fun load(id: Long) {
        exerciseId.value = id
    }
}
