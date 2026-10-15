package com.gymsesh.app.data.repository

import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

/** Domain access to the exercise catalogue. */
interface ExerciseRepository {
    fun observeExercises(): Flow<List<Exercise>>
    fun observeExercise(id: Long): Flow<Exercise?>
    suspend fun getExercise(id: Long): Exercise?
    fun searchExercises(query: String): Flow<List<Exercise>>
    fun observeByPrimaryMuscle(muscle: MuscleGroup): Flow<List<Exercise>>
    suspend fun count(): Int
    suspend fun upsert(exercise: Exercise): Long
    suspend fun insertAll(exercises: List<Exercise>): List<Long>
    suspend fun update(exercise: Exercise)
    suspend fun delete(exercise: Exercise)
}
