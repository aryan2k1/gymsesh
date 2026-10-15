package com.gymsesh.app.data.repository

import com.gymsesh.app.data.db.dao.SplitTemplateDao
import com.gymsesh.app.data.db.entities.SplitTemplate
import com.gymsesh.app.data.db.entities.TemplateDay
import com.gymsesh.app.data.db.entities.TemplateExercise
import com.gymsesh.app.data.db.relations.TemplateWithDays
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplitRepositoryImpl @Inject constructor(
    private val dao: SplitTemplateDao,
) : SplitRepository {

    override fun observeTemplates(): Flow<List<SplitTemplate>> =
        dao.observeTemplates().flowOn(Dispatchers.IO)

    override fun observeTemplatesWithDays(): Flow<List<TemplateWithDays>> =
        dao.observeTemplatesWithDays().flowOn(Dispatchers.IO)

    override fun observeTemplateWithDays(templateId: Long): Flow<TemplateWithDays?> =
        dao.observeTemplateWithDays(templateId).flowOn(Dispatchers.IO)

    override fun observeDays(templateId: Long): Flow<List<TemplateDay>> =
        dao.observeDays(templateId).flowOn(Dispatchers.IO)

    override fun observeDayExercises(dayId: Long): Flow<List<TemplateExercise>> =
        dao.observeExercises(dayId).flowOn(Dispatchers.IO)

    override suspend fun insertTemplate(template: SplitTemplate): Long =
        withContext(Dispatchers.IO) { dao.insertTemplate(template) }

    override suspend fun insertDay(day: TemplateDay): Long =
        withContext(Dispatchers.IO) { dao.insertDay(day) }

    override suspend fun insertDays(days: List<TemplateDay>): List<Long> =
        withContext(Dispatchers.IO) { dao.insertDays(days) }

    override suspend fun insertExercise(exercise: TemplateExercise): Long =
        withContext(Dispatchers.IO) { dao.insertExercise(exercise) }

    override suspend fun insertExercises(exercises: List<TemplateExercise>): List<Long> =
        withContext(Dispatchers.IO) { dao.insertExercises(exercises) }

    override suspend fun updateTemplate(template: SplitTemplate) =
        withContext(Dispatchers.IO) { dao.updateTemplate(template) }

    override suspend fun updateDay(day: TemplateDay) =
        withContext(Dispatchers.IO) { dao.updateDay(day) }

    override suspend fun updateExercise(exercise: TemplateExercise) =
        withContext(Dispatchers.IO) { dao.updateExercise(exercise) }

    override suspend fun deleteTemplate(template: SplitTemplate) =
        withContext(Dispatchers.IO) { dao.deleteTemplate(template) }

    override suspend fun deleteDay(day: TemplateDay) =
        withContext(Dispatchers.IO) { dao.deleteDay(day) }

    override suspend fun deleteExercise(exercise: TemplateExercise) =
        withContext(Dispatchers.IO) { dao.deleteExercise(exercise) }
}
