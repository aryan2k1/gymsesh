package com.gymsesh.app.data.repository

import com.gymsesh.app.data.db.entities.LoggedExercise
import com.gymsesh.app.data.db.entities.LoggedSet
import com.gymsesh.app.data.db.entities.WorkoutSession
import com.gymsesh.app.data.db.relations.SessionWithExercises
import com.gymsesh.app.data.model.SetWithMuscles
import kotlinx.coroutines.flow.Flow

/** Domain access to logged workout sessions, exercises and sets. */
interface WorkoutRepository {
    fun observeSessions(): Flow<List<WorkoutSession>>
    fun observeSessionsWithExercises(): Flow<List<SessionWithExercises>>
    fun observeSessionWithExercises(sessionId: Long): Flow<SessionWithExercises?>
    fun observeSessionsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<WorkoutSession>>
    fun observeLoggedExercises(sessionId: Long): Flow<List<LoggedExercise>>
    fun observeSets(loggedExerciseId: Long): Flow<List<LoggedSet>>

    /** Raw feed for the muscle-group pie chart over an inclusive epoch-day window. */
    fun observeSetsWithMusclesInRange(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<SetWithMuscles>>

    /**
     * Atomically persists a whole workout (session + its exercises + their sets)
     * in a single Room transaction and returns the new session id. Prefer this
     * over the individual insert* calls when saving a complete workout.
     */
    suspend fun saveSession(session: WorkoutSession, exercises: List<NewLoggedExercise>): Long

    suspend fun insertSession(session: WorkoutSession): Long
    suspend fun insertLoggedExercise(loggedExercise: LoggedExercise): Long
    suspend fun insertSet(set: LoggedSet): Long
    suspend fun insertSets(sets: List<LoggedSet>): List<Long>

    suspend fun updateSession(session: WorkoutSession)
    suspend fun updateLoggedExercise(loggedExercise: LoggedExercise)
    suspend fun updateSet(set: LoggedSet)

    suspend fun deleteSession(session: WorkoutSession)
    suspend fun deleteLoggedExercise(loggedExercise: LoggedExercise)
    suspend fun deleteSet(set: LoggedSet)
}
