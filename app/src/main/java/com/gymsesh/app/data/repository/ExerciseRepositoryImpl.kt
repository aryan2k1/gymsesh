package com.gymsesh.app.data.repository

import com.gymsesh.app.data.db.dao.ExerciseDao
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.model.MuscleGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
) : ExerciseRepository {

    override fun observeExercises(): Flow<List<Exercise>> =
        dao.observeAll().flowOn(Dispatchers.IO)

    override fun observeExercise(id: Long): Flow<Exercise?> =
        dao.observeById(id).flowOn(Dispatchers.IO)

    override suspend fun getExercise(id: Long): Exercise? =
        withContext(Dispatchers.IO) { dao.getById(id) }

    override fun searchExercises(query: String): Flow<List<Exercise>> =
        dao.search(query).flowOn(Dispatchers.IO)

    override fun observeByPrimaryMuscle(muscle: MuscleGroup): Flow<List<Exercise>> =
        dao.observeByPrimaryMuscle(muscle.name).flowOn(Dispatchers.IO)

    override suspend fun count(): Int =
        withContext(Dispatchers.IO) { dao.count() }

    override suspend fun upsert(exercise: Exercise): Long =
        withContext(Dispatchers.IO) { dao.insert(exercise) }

    override suspend fun insertAll(exercises: List<Exercise>): List<Long> =
        withContext(Dispatchers.IO) { dao.insertAll(exercises) }

    override suspend fun update(exercise: Exercise) =
        withContext(Dispatchers.IO) { dao.update(exercise) }

    override suspend fun delete(exercise: Exercise) =
        withContext(Dispatchers.IO) { dao.delete(exercise) }
}
