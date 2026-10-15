package com.gymsesh.app.ui.screens.splits

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsesh.app.data.db.entities.SplitTemplate
import com.gymsesh.app.data.db.entities.TemplateDay
import com.gymsesh.app.data.db.entities.TemplateExercise
import com.gymsesh.app.data.db.relations.TemplateWithDays
import com.gymsesh.app.data.model.SplitType
import com.gymsesh.app.data.prefs.SplitPreferences
import com.gymsesh.app.data.repository.ExerciseRepository
import com.gymsesh.app.data.repository.SplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One exercise line within a split day (name resolved for display). */
@Immutable
data class SplitExerciseLine(
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
)

/** One day of a split, with its ordered exercises. */
@Immutable
data class SplitDayCard(
    val dayName: String,
    val exercises: List<SplitExerciseLine>,
)

/** A split template card for the list. */
@Immutable
data class SplitCard(
    val id: Long,
    val name: String,
    val type: SplitType,
    val days: List<SplitDayCard>,
)

/** Immutable UI state for the Splits tab. */
@Immutable
data class SplitsUiState(
    val templates: List<SplitCard> = emptyList(),
    val activeTemplateId: Long? = null,
    val rotationIndex: Int = 0,
)

/**
 * Backs [SplitsScreen]. Observes all templates with their days/exercises,
 * resolves exercise names for display, and exposes the active split selection.
 * Lets the user set the active split, set the rotation day, and clone a template
 * into an editable CUSTOM copy.
 */
@HiltViewModel
class SplitsViewModel @Inject constructor(
    private val splitRepository: SplitRepository,
    private val exerciseRepository: ExerciseRepository,
    private val splitPreferences: SplitPreferences,
) : ViewModel() {

    val state: StateFlow<SplitsUiState> = combine(
        splitRepository.observeTemplatesWithDays(),
        exerciseRepository.observeExercises(),
        splitPreferences.activeTemplateId,
        splitPreferences.rotationIndex,
    ) { templates, exercises, activeId, rotation ->
        val nameById = exercises.associate { it.id to it.name }
        SplitsUiState(
            templates = templates.map { it.toCard(nameById) },
            activeTemplateId = activeId,
            rotationIndex = rotation,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SplitsUiState(),
    )

    /** Makes [templateId] the active split (resets rotation to day one). */
    fun selectActive(templateId: Long) {
        viewModelScope.launch { splitPreferences.setActiveTemplate(templateId) }
    }

    /** Sets which day of the active split counts as "today". */
    fun setRotationDay(index: Int) {
        viewModelScope.launch { splitPreferences.setRotationIndex(index) }
    }

    /** Advances the active split's rotation to the next day (wraps around). */
    fun advanceDay() {
        val current = state.value
        val active = current.activeTemplateId ?: return
        val count = current.templates.firstOrNull { it.id == active }?.days?.size ?: return
        if (count <= 0) return
        val next = ((current.rotationIndex % count) + count) % count
        setRotationDay((next + 1) % count)
    }

    /**
     * Clones [templateId] into a new `type = CUSTOM` copy (days + exercises
     * duplicated). The copy is independent and can be freely edited later.
     */
    fun clone(templateId: Long) {
        viewModelScope.launch {
            val source = splitRepository.observeTemplateWithDays(templateId).first() ?: return@launch
            val newId = splitRepository.insertTemplate(
                SplitTemplate(name = "${source.template.name} (Copy)", type = SplitType.CUSTOM),
            )
            source.days.sortedBy { it.day.order }.forEachIndexed { index, dayWithEx ->
                val dayId = splitRepository.insertDay(
                    TemplateDay(
                        templateId = newId,
                        dayName = dayWithEx.day.dayName,
                        order = index,
                    ),
                )
                val slots = dayWithEx.exercises.map { ex ->
                    TemplateExercise(
                        dayId = dayId,
                        exerciseId = ex.exerciseId,
                        targetSets = ex.targetSets,
                        targetReps = ex.targetReps,
                    )
                }
                if (slots.isNotEmpty()) splitRepository.insertExercises(slots)
            }
        }
    }

    private companion object {
        fun TemplateWithDays.toCard(nameById: Map<Long, String>): SplitCard = SplitCard(
            id = template.id,
            name = template.name,
            type = template.type,
            days = days.sortedBy { it.day.order }.map { dayWithEx ->
                SplitDayCard(
                    dayName = dayWithEx.day.dayName,
                    exercises = dayWithEx.exercises.map { ex ->
                        SplitExerciseLine(
                            name = nameById[ex.exerciseId] ?: "#${ex.exerciseId}",
                            targetSets = ex.targetSets,
                            targetReps = ex.targetReps,
                        )
                    },
                )
            },
        )
    }
}
