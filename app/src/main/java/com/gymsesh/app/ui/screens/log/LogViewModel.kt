package com.gymsesh.app.ui.screens.log

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.db.entities.WorkoutSession
import com.gymsesh.app.data.repository.ExerciseRepository
import com.gymsesh.app.data.repository.NewLoggedExercise
import com.gymsesh.app.data.repository.NewSet
import com.gymsesh.app.data.repository.SplitRepository
import com.gymsesh.app.data.repository.WorkoutRepository
import com.gymsesh.app.domain.PendingWorkoutStore
import androidx.glance.appwidget.updateAll
import com.gymsesh.app.widget.TodayWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/**
 * Drives the in-progress workout logging flow (see BUILD_BIBLE §4).
 *
 * A session is held entirely in memory as [ActiveSession] while the user edits
 * it, then flattened into [WorkoutSession] → [LoggedExercise] → [LoggedSet] and
 * persisted in order on [finishWorkout]. Reps / weight / RPE are stored as raw
 * strings during editing (so partial input never fights the text fields) and
 * parsed at save time.
 */
@HiltViewModel
class LogViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val splitRepository: SplitRepository,
    private val pendingWorkoutStore: PendingWorkoutStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    /** All catalogue exercises, for the in-sheet picker. */
    val exercises: StateFlow<List<Exercise>> = exerciseRepository.observeExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // Monotonic local ids so Compose can key rows before Room assigns real ones.
    private val localIds = AtomicLong(0L)
    private fun nextId(): Long = localIds.incrementAndGet()

    // --- Session lifecycle ---------------------------------------------------

    fun startWorkout() {
        if (_uiState.value.session != null) return
        _uiState.update {
            it.copy(session = ActiveSession(dateEpochDay = LocalDate.now().toEpochDay()))
        }
    }

    /**
     * If the Today tab requested a pre-filled start (via [PendingWorkoutStore])
     * and no session is already active, builds an [ActiveSession] from that
     * split day: one exercise entry per [TemplateExercise], each with
     * `targetSets` sets pre-filled with `targetReps` reps and an EMPTY weight
     * (the user only types the load). The template-day id is remembered on the
     * session so [finishWorkout] can link the saved workout back to the split.
     *
     * If a session is already in progress, the pending request is simply
     * cleared so it never overwrites in-progress work. Idempotent per request
     * (the store clears on consume), so a re-composition can't double-fire it.
     */
    fun consumePendingStart() {
        // Already logging: drop the request so it can't clobber live edits.
        if (_uiState.value.session != null) {
            pendingWorkoutStore.consume()
            return
        }
        val dayId = pendingWorkoutStore.consume() ?: return
        viewModelScope.launch {
            val slots = splitRepository.observeDayExercises(dayId).first()
            val nameById = exerciseRepository.observeExercises().first()
                .associate { it.id to it.name }
            val entries = slots.map { slot ->
                ExerciseEntry(
                    uid = nextId(),
                    exerciseId = slot.exerciseId,
                    name = nameById[slot.exerciseId] ?: "#${slot.exerciseId}",
                    sets = List(slot.targetSets.coerceAtLeast(1)) {
                        SetEntry(uid = nextId(), reps = slot.targetReps.toString())
                    },
                )
            }
            _uiState.update { state ->
                // Guard against a race: a manual start may have landed meanwhile.
                if (state.session != null) return@update state
                state.copy(
                    session = ActiveSession(
                        dateEpochDay = LocalDate.now().toEpochDay(),
                        templateDayId = dayId,
                        exercises = entries,
                    ),
                )
            }
        }
    }

    fun discardWorkout() {
        _uiState.update { it.copy(session = null, isPickerOpen = false) }
    }

    fun finishWorkout() {
        val active = _uiState.value.session ?: return
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val exercises = active.exercises.mapIndexed { index, ex ->
                    NewLoggedExercise(
                        exerciseId = ex.exerciseId,
                        order = index,
                        sets = ex.sets.map { set ->
                            NewSet(
                                reps = set.reps.toIntOrNull() ?: 0,
                                weightKg = set.weightKg.toDoubleOrNull() ?: 0.0,
                                rpe = set.rpe.toDoubleOrNull(),
                                isWarmup = set.isWarmup,
                                isDone = set.isDone,
                            )
                        },
                    )
                }
                // Single atomic write: commits the whole session or nothing.
                workoutRepository.saveSession(
                    WorkoutSession(
                        dateEpochDay = active.dateEpochDay,
                        templateDayId = active.templateDayId,
                    ),
                    exercises,
                )
                // Refresh the home-screen widget; never let a widget failure
                // undo a successful save.
                try {
                    TodayWidget().updateAll(appContext)
                } catch (_: Throwable) {
                    // Widget refresh is best-effort.
                }
                _uiState.update { LogUiState() }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    // --- Exercise picker -----------------------------------------------------

    fun openPicker() = _uiState.update { it.copy(isPickerOpen = true, pickerQuery = "") }

    fun closePicker() = _uiState.update { it.copy(isPickerOpen = false) }

    fun setPickerQuery(query: String) = _uiState.update { it.copy(pickerQuery = query) }

    fun addExercise(exercise: Exercise) {
        _uiState.update { state ->
            val session = state.session ?: return@update state
            val added = ExerciseEntry(
                uid = nextId(),
                exerciseId = exercise.id,
                name = exercise.name,
                sets = listOf(newSet()),
            )
            state.copy(
                session = session.copy(exercises = session.exercises + added),
                isPickerOpen = false,
            )
        }
    }

    fun removeExercise(exerciseUid: Long) = mutateSession { session ->
        session.copy(exercises = session.exercises.filterNot { it.uid == exerciseUid })
    }

    // --- Set editing ---------------------------------------------------------

    fun addSet(exerciseUid: Long) = mutateExercise(exerciseUid) { entry ->
        entry.copy(sets = entry.sets + newSet())
    }

    fun removeSet(exerciseUid: Long, setUid: Long) = mutateExercise(exerciseUid) { entry ->
        entry.copy(sets = entry.sets.filterNot { it.uid == setUid })
    }

    fun updateReps(exerciseUid: Long, setUid: Long, reps: String) =
        mutateSet(exerciseUid, setUid) { it.copy(reps = reps.filter(Char::isDigit)) }

    fun updateWeight(exerciseUid: Long, setUid: Long, weight: String) =
        mutateSet(exerciseUid, setUid) { it.copy(weightKg = weight.sanitizeDecimal()) }

    fun updateRpe(exerciseUid: Long, setUid: Long, rpe: String) =
        mutateSet(exerciseUid, setUid) { it.copy(rpe = rpe.sanitizeDecimal()) }

    fun toggleSetDone(exerciseUid: Long, setUid: Long, done: Boolean) =
        mutateSet(exerciseUid, setUid) { it.copy(isDone = done) }

    fun toggleWarmup(exerciseUid: Long, setUid: Long, warmup: Boolean) =
        mutateSet(exerciseUid, setUid) { it.copy(isWarmup = warmup) }

    // --- Helpers -------------------------------------------------------------

    private fun newSet() = SetEntry(uid = nextId())

    private inline fun mutateSession(transform: (ActiveSession) -> ActiveSession) {
        _uiState.update { state ->
            val session = state.session ?: return@update state
            state.copy(session = transform(session))
        }
    }

    private inline fun mutateExercise(
        exerciseUid: Long,
        transform: (ExerciseEntry) -> ExerciseEntry,
    ) = mutateSession { session ->
        session.copy(
            exercises = session.exercises.map { if (it.uid == exerciseUid) transform(it) else it },
        )
    }

    private inline fun mutateSet(
        exerciseUid: Long,
        setUid: Long,
        transform: (SetEntry) -> SetEntry,
    ) = mutateExercise(exerciseUid) { entry ->
        entry.copy(sets = entry.sets.map { if (it.uid == setUid) transform(it) else it })
    }
}

/** Keep at most one decimal point and only digits / dot. */
private fun String.sanitizeDecimal(): String {
    val filtered = filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    if (firstDot == -1) return filtered
    return filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
}

@Immutable
data class LogUiState(
    val session: ActiveSession? = null,
    val isPickerOpen: Boolean = false,
    val pickerQuery: String = "",
    val isSaving: Boolean = false,
)

@Immutable
data class ActiveSession(
    val dateEpochDay: Long,
    /** Split day this session was pre-filled from, for history linkage. */
    val templateDayId: Long? = null,
    val exercises: List<ExerciseEntry> = emptyList(),
)

@Immutable
data class ExerciseEntry(
    val uid: Long,
    val exerciseId: Long,
    val name: String,
    val sets: List<SetEntry> = emptyList(),
)

@Immutable
data class SetEntry(
    val uid: Long,
    val reps: String = "",
    val weightKg: String = "",
    val rpe: String = "",
    val isDone: Boolean = false,
    val isWarmup: Boolean = false,
)
