package com.gymsesh.app.data.repository

import com.gymsesh.app.data.db.dao.WorkoutDao
import com.gymsesh.app.data.db.entities.LoggedExercise
import com.gymsesh.app.data.db.entities.LoggedSet
import com.gymsesh.app.data.db.entities.WorkoutSession
import com.gymsesh.app.data.db.relations.SessionWithExercises
import com.gymsesh.app.data.model.SetWithMuscles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao,
) : WorkoutRepository {

    override fun observeSessions(): Flow<List<WorkoutSession>> =
        dao.observeSessions().flowOn(Dispatchers.IO)

    override fun observeSessionsWithExercises(): Flow<List<SessionWithExercises>> =
        dao.observeSessionsWithExercises().flowOn(Dispatchers.IO)

    override fun observeSessionWithExercises(sessionId: Long): Flow<SessionWithExercises?> =
        dao.observeSessionWithExercises(sessionId).flowOn(Dispatchers.IO)

    override fun observeSessionsInRange(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<WorkoutSession>> =
        dao.observeSessionsInRange(startEpochDay, endEpochDay).flowOn(Dispatchers.IO)

    override fun observeLoggedExercises(sessionId: Long): Flow<List<LoggedExercise>> =
        dao.observeLoggedExercises(sessionId).flowOn(Dispatchers.IO)

    override fun observeSets(loggedExerciseId: Long): Flow<List<LoggedSet>> =
        dao.observeSets(loggedExerciseId).flowOn(Dispatchers.IO)

    override fun observeSetsWithMusclesInRange(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<SetWithMuscles>> =
        dao.observeSetsWithMusclesInRange(startEpochDay, endEpochDay).flowOn(Dispatchers.IO)

    override suspend fun saveSession(
        session: WorkoutSession,
        exercises: List<NewLoggedExercise>,
    ): Long = withContext(Dispatchers.IO) { dao.saveSessionGraph(session, exercises) }

    override suspend fun insertSession(session: WorkoutSession): Long =
        withContext(Dispatchers.IO) { dao.insertSession(session) }

    override suspend fun insertLoggedExercise(loggedExercise: LoggedExercise): Long =
        withContext(Dispatchers.IO) { dao.insertLoggedExercise(loggedExercise) }

    override suspend fun insertSet(set: LoggedSet): Long =
        withContext(Dispatchers.IO) { dao.insertSet(set) }

    override suspend fun insertSets(sets: List<LoggedSet>): List<Long> =
        withContext(Dispatchers.IO) { dao.insertSets(sets) }

    override suspend fun updateSession(session: WorkoutSession) =
        withContext(Dispatchers.IO) { dao.updateSession(session) }

    override suspend fun updateLoggedExercise(loggedExercise: LoggedExercise) =
        withContext(Dispatchers.IO) { dao.updateLoggedExercise(loggedExercise) }

    override suspend fun updateSet(set: LoggedSet) =
        withContext(Dispatchers.IO) { dao.updateSet(set) }

    override suspend fun deleteSession(session: WorkoutSession) =
        withContext(Dispatchers.IO) { dao.deleteSession(session) }

    override suspend fun deleteLoggedExercise(loggedExercise: LoggedExercise) =
        withContext(Dispatchers.IO) { dao.deleteLoggedExercise(loggedExercise) }

    override suspend fun deleteSet(set: LoggedSet) =
        withContext(Dispatchers.IO) { dao.deleteSet(set) }
}
