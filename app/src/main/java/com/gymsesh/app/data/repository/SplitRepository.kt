package com.gymsesh.app.data.repository

import com.gymsesh.app.data.db.entities.SplitTemplate
import com.gymsesh.app.data.db.entities.TemplateDay
import com.gymsesh.app.data.db.entities.TemplateExercise
import com.gymsesh.app.data.db.relations.TemplateWithDays
import kotlinx.coroutines.flow.Flow

/** Domain access to split templates and their days/exercises. */
interface SplitRepository {
    fun observeTemplates(): Flow<List<SplitTemplate>>
    fun observeTemplatesWithDays(): Flow<List<TemplateWithDays>>
    fun observeTemplateWithDays(templateId: Long): Flow<TemplateWithDays?>
    fun observeDays(templateId: Long): Flow<List<TemplateDay>>
    fun observeDayExercises(dayId: Long): Flow<List<TemplateExercise>>

    suspend fun insertTemplate(template: SplitTemplate): Long
    suspend fun insertDay(day: TemplateDay): Long
    suspend fun insertDays(days: List<TemplateDay>): List<Long>
    suspend fun insertExercise(exercise: TemplateExercise): Long
    suspend fun insertExercises(exercises: List<TemplateExercise>): List<Long>

    suspend fun updateTemplate(template: SplitTemplate)
    suspend fun updateDay(day: TemplateDay)
    suspend fun updateExercise(exercise: TemplateExercise)

    suspend fun deleteTemplate(template: SplitTemplate)
    suspend fun deleteDay(day: TemplateDay)
    suspend fun deleteExercise(exercise: TemplateExercise)
}
