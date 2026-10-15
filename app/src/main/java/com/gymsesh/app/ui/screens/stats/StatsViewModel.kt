package com.gymsesh.app.ui.screens.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsesh.app.data.db.relations.SessionWithExercises
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.data.model.SetWithMuscles
import com.gymsesh.app.data.repository.ExerciseRepository
import com.gymsesh.app.data.repository.WorkoutRepository
import com.gymsesh.app.domain.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/** The selectable time window for the muscle-group pie chart. */
enum class StatsWindow { THIS_WEEK, THIS_MONTH, ALL_TIME }

/** One slice of the muscle-group pie: a [MuscleGroup] and its share of trained sets. */
@Immutable
data class MuscleSlice(
    val muscle: MuscleGroup,
    /** Weighted set count: primary = 1.0, secondary = 0.5, warm-ups excluded. May be fractional. */
    val weightedSets: Double,
    /** Share of the window's total weighted sets, in 0f..1f. */
    val fraction: Float,
)

/** One logged set within an [ExerciseDetail], shown when a history row is expanded. */
@Immutable
data class SetLine(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val isWarmup: Boolean,
)

/** One exercise (resolved name + its sets) within an expanded [SessionSummary]. */
@Immutable
data class ExerciseDetail(
    val exerciseName: String,
    val sets: List<SetLine>,
)

/**
 * A single row in the workout history list, newest first. [exercises] carries the
 * per-exercise / per-set detail revealed when the row is expanded in place.
 */
@Immutable
data class SessionSummary(
    val sessionId: Long,
    val dateEpochDay: Long,
    val exerciseCount: Int,
    val totalSets: Int,
    /** Total volume = sum of reps × weightKg over every set in the session. */
    val totalVolumeKg: Double,
    val exercises: List<ExerciseDetail> = emptyList(),
)

/** A personal record: the best working set for an exercise by estimated 1RM. */
@Immutable
data class PrEntry(
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    /** Epley estimated one-rep max: weight × (1 + reps / 30). */
    val est1RM: Double,
)

/**
 * Immutable UI state for the Stats/Insight screen.
 *
 * [muscleSlices] is windowed by [window]; [sessions] (history) and [prs]
 * (personal records) span all logged data.
 */
@Immutable
data class StatsUiState(
    val window: StatsWindow = StatsWindow.THIS_WEEK,
    val muscleSlices: List<MuscleSlice> = emptyList(),
    val sessions: List<SessionSummary> = emptyList(),
    val prs: List<PrEntry> = emptyList(),
    /** Distinct epoch-days with >= 1 logged workout — powers the calendar highlight. */
    val workoutDays: Set<Long> = emptySet(),
    /** Consecutive-days "don't break the chain" streak (see [StreakCalculator]). */
    val currentStreak: Int = 0,
)

/**
 * Backs [StatsScreen]. Aggregates the muscle-group pie (weighted, warm-up-excluded,
 * windowed), the recent workout history and per-exercise personal records into a
 * single immutable [StatsUiState] exposed as a [StateFlow].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val window = MutableStateFlow(StatsWindow.THIS_WEEK)

    val state: StateFlow<StatsUiState> = window.flatMapLatest { selected ->
        val (start, end) = selected.epochDayRange()
        combine(
            workoutRepository.observeSetsWithMusclesInRange(start, end),
            workoutRepository.observeSessionsWithExercises(),
            workoutRepository.observeSessions(),
            exerciseRepository.observeExercises(),
        ) { sets, sessionsWithExercises, sessions, exercises ->
            val names = exercises.associate { it.id to it.name }
            val workoutDays = sessions.mapTo(mutableSetOf()) { it.dateEpochDay }
            StatsUiState(
                window = selected,
                muscleSlices = aggregateMuscles(sets),
                sessions = summariseSessions(sessionsWithExercises, names),
                prs = detectPrs(sessionsWithExercises, names),
                workoutDays = workoutDays,
                currentStreak = StreakCalculator.currentStreak(
                    workoutDays = workoutDays,
                    todayEpoch = LocalDate.now().toEpochDay(),
                ),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    fun setWindow(value: StatsWindow) {
        window.value = value
    }

    private companion object {
        /** How many top personal records to surface. */
        const val MAX_PRS = 5

        /** Fallback label if an exercise id can't be resolved (e.g. deleted exercise). */
        const val UNKNOWN_EXERCISE = "Exercise"

        /**
         * Weighted set counts per muscle group over the window: each non-warm-up set
         * adds 1.0 to its exercise's primary muscle and 0.5 to each secondary muscle.
         * Returned sorted by weighted count descending, with each slice's [MuscleSlice.fraction].
         */
        fun aggregateMuscles(sets: List<SetWithMuscles>): List<MuscleSlice> {
            val weights = linkedMapOf<MuscleGroup, Double>()
            for (set in sets) {
                if (set.isWarmup) continue
                weights.merge(set.primaryMuscle, 1.0, Double::plus)
                for (secondary in set.secondaryMuscles) {
                    weights.merge(secondary, 0.5, Double::plus)
                }
            }
            val total = weights.values.sum()
            if (total <= 0.0) return emptyList()
            return weights.entries
                .sortedByDescending { it.value }
                .map { (muscle, weighted) ->
                    MuscleSlice(
                        muscle = muscle,
                        weightedSets = weighted,
                        fraction = (weighted / total).toFloat(),
                    )
                }
        }

        /**
         * One [SessionSummary] per session, newest first. Each summary also carries its
         * ordered [ExerciseDetail]s (name resolved via [names], sets ordered by set number)
         * so a history row can expand in place without another round-trip.
         */
        fun summariseSessions(
            sessions: List<SessionWithExercises>,
            names: Map<Long, String>,
        ): List<SessionSummary> =
            sessions.map { swe ->
                val allSets = swe.exercises.flatMap { it.sets }
                val details = swe.exercises
                    .sortedBy { it.loggedExercise.order }
                    .map { ex ->
                        ExerciseDetail(
                            exerciseName = names[ex.loggedExercise.exerciseId]
                                ?: UNKNOWN_EXERCISE,
                            sets = ex.sets
                                .sortedBy { it.setNumber }
                                .map { set ->
                                    SetLine(
                                        setNumber = set.setNumber,
                                        reps = set.reps,
                                        weightKg = set.weightKg,
                                        isWarmup = set.isWarmup,
                                    )
                                },
                        )
                    }
                SessionSummary(
                    sessionId = swe.session.id,
                    dateEpochDay = swe.session.dateEpochDay,
                    exerciseCount = swe.exercises.size,
                    totalSets = allSets.size,
                    totalVolumeKg = allSets.sumOf { it.reps * it.weightKg },
                    exercises = details,
                )
            }.sortedWith(
                compareByDescending<SessionSummary> { it.dateEpochDay }
                    .thenByDescending { it.sessionId },
            )

        /**
         * Best working set per exercise by Epley estimated 1RM (weight × (1 + reps / 30)),
         * across every logged set. Warm-ups and empty (zero weight/reps) sets are ignored.
         * Returned as the top [MAX_PRS] by estimated 1RM, descending.
         */
        fun detectPrs(
            sessions: List<SessionWithExercises>,
            names: Map<Long, String>,
        ): List<PrEntry> {
            val bestByExercise = linkedMapOf<Long, PrEntry>()
            for (session in sessions) {
                for (exercise in session.exercises) {
                    val exerciseId = exercise.loggedExercise.exerciseId
                    val name = names[exerciseId] ?: continue
                    for (set in exercise.sets) {
                        if (set.isWarmup || set.reps <= 0 || set.weightKg <= 0.0) continue
                        val est = set.weightKg * (1.0 + set.reps / 30.0)
                        val current = bestByExercise[exerciseId]
                        if (current == null || est > current.est1RM) {
                            bestByExercise[exerciseId] = PrEntry(
                                exerciseName = name,
                                weightKg = set.weightKg,
                                reps = set.reps,
                                est1RM = est,
                            )
                        }
                    }
                }
            }
            return bestByExercise.values
                .sortedByDescending { it.est1RM }
                .take(MAX_PRS)
        }
    }
}

/**
 * Inclusive `start..end` epoch-day range for this window, relative to today.
 * THIS_WEEK starts on the most recent Monday; THIS_MONTH on the 1st; ALL_TIME at epoch day 0.
 */
private fun StatsWindow.epochDayRange(): Pair<Long, Long> {
    val today = LocalDate.now()
    val end = today.toEpochDay()
    val start = when (this) {
        StatsWindow.THIS_WEEK -> today.minusDays((today.dayOfWeek.value - 1).toLong())
        StatsWindow.THIS_MONTH -> today.withDayOfMonth(1)
        StatsWindow.ALL_TIME -> return 0L to end
    }.toEpochDay()
    return start to end
}
