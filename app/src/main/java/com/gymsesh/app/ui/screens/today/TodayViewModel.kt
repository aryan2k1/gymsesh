package com.gymsesh.app.ui.screens.today

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsesh.app.data.db.relations.TemplateWithDays
import com.gymsesh.app.data.prefs.SplitPreferences
import com.gymsesh.app.data.repository.ExerciseRepository
import com.gymsesh.app.data.repository.SplitRepository
import com.gymsesh.app.data.repository.WorkoutRepository
import com.gymsesh.app.domain.PendingWorkoutStore
import com.gymsesh.app.domain.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** One exercise line of today's session. */
@Immutable
data class TodayExercise(
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
)

/** Immutable UI state for the Today tab. */
@Immutable
data class TodayUiState(
    val hasActiveSplit: Boolean = false,
    val splitName: String = "",
    val dayName: String = "",
    val dayCount: Int = 0,
    /** 1-based position of today's day, for display. */
    val dayPosition: Int = 0,
    val exercises: List<TodayExercise> = emptyList(),
    /** Template-day id backing today's session, for pre-filling the Log tab. */
    val currentDayId: Long? = null,
    /** Current "don't break the chain" gym streak in days (0 = none). */
    val streak: Int = 0,
)

/**
 * Backs [TodayScreen]. Derives "today's session" from the active split template
 * (from [SplitPreferences]) and the rotation index: today's [TemplateWithDays]
 * day is `days[rotationIndex mod dayCount]` (days ordered by `order`). Each
 * template-exercise id is resolved to its catalogue name for display. Also
 * surfaces the gym streak computed from logged sessions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    splitRepository: SplitRepository,
    exerciseRepository: ExerciseRepository,
    workoutRepository: WorkoutRepository,
    private val splitPreferences: SplitPreferences,
    private val pendingWorkoutStore: PendingWorkoutStore,
) : ViewModel() {

    private val activeTemplate = splitPreferences.activeTemplateId.flatMapLatest { id ->
        if (id == null) flowOf(null) else splitRepository.observeTemplateWithDays(id)
    }

    private val streak = workoutRepository.observeSessions().map { sessions ->
        val days = sessions.map { it.dateEpochDay }.toSet()
        StreakCalculator.currentStreak(days, LocalDate.now().toEpochDay())
    }

    val state: StateFlow<TodayUiState> = combine(
        activeTemplate,
        splitPreferences.rotationIndex,
        exerciseRepository.observeExercises(),
        streak,
    ) { template, rotation, exercises, streakDays ->
        val nameById = exercises.associate { it.id to it.name }
        buildState(template, rotation, nameById, streakDays)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    /** Advances the rotation to the next day of the active split (wraps around). */
    fun advanceDay() {
        val current = state.value
        val count = current.dayCount
        if (!current.hasActiveSplit || count <= 0) return
        // dayPosition is 1-based; the next 0-based index wraps within the split.
        val nextIndex = current.dayPosition % count
        viewModelScope.launch { splitPreferences.setRotationIndex(nextIndex) }
    }

    /**
     * Called just before navigating to Log. If a split is active, records
     * today's template-day so the Log tab can pre-fill the session from it.
     */
    fun prepareStart() {
        val current = state.value
        val dayId = current.currentDayId
        if (current.hasActiveSplit && dayId != null) {
            pendingWorkoutStore.request(dayId)
        }
    }

    private companion object {
        fun buildState(
            template: TemplateWithDays?,
            rotation: Int,
            nameById: Map<Long, String>,
            streakDays: Int,
        ): TodayUiState {
            if (template == null || template.days.isEmpty()) {
                return TodayUiState(streak = streakDays)
            }
            val days = template.days.sortedBy { it.day.order }
            val count = days.size
            val index = ((rotation % count) + count) % count
            val today = days[index]
            val exercises = today.exercises.map { slot ->
                TodayExercise(
                    name = nameById[slot.exerciseId] ?: "#${slot.exerciseId}",
                    targetSets = slot.targetSets,
                    targetReps = slot.targetReps,
                )
            }
            return TodayUiState(
                hasActiveSplit = true,
                splitName = template.template.name,
                dayName = today.day.dayName,
                dayCount = count,
                dayPosition = index + 1,
                exercises = exercises,
                currentDayId = today.day.id,
                streak = streakDays,
            )
        }
    }
}
