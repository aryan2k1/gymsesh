package com.gymsesh.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymsesh.app.data.db.entities.LoggedExercise
import com.gymsesh.app.data.db.entities.LoggedSet
import com.gymsesh.app.data.db.entities.WorkoutSession
import com.gymsesh.app.data.db.relations.SessionWithExercises
import com.gymsesh.app.data.model.SetWithMuscles
import com.gymsesh.app.data.repository.NewLoggedExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout_sessions ORDER BY dateEpochDay DESC, id DESC")
    fun observeSessions(): Flow<List<WorkoutSession>>

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY dateEpochDay DESC, id DESC")
    fun observeSessionsWithExercises(): Flow<List<SessionWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun observeSessionWithExercises(sessionId: Long): Flow<SessionWithExercises?>

    @Query(
        "SELECT * FROM workout_sessions " +
            "WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay " +
            "ORDER BY dateEpochDay DESC, id DESC"
    )
    fun observeSessionsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM logged_exercises WHERE sessionId = :sessionId ORDER BY position ASC")
    fun observeLoggedExercises(sessionId: Long): Flow<List<LoggedExercise>>

    @Query("SELECT * FROM logged_sets WHERE loggedExerciseId = :loggedExerciseId ORDER BY setNumber ASC")
    fun observeSets(loggedExerciseId: Long): Flow<List<LoggedSet>>

    /**
     * Feeds the muscle-group pie chart: every logged set within the given
     * [startEpochDay]..[endEpochDay] window (inclusive) joined with its
     * exercise's primary and secondary muscle groups. Aggregation (primary =
     * full, secondary = half; optionally excluding warm-ups) is done in the
     * repository/domain layer.
     */
    @Query(
        "SELECT e.primaryMuscle AS primaryMuscle, " +
            "e.secondaryMuscles AS secondaryMuscles, " +
            "s.isWarmup AS isWarmup " +
            "FROM logged_sets s " +
            "INNER JOIN logged_exercises le ON s.loggedExerciseId = le.id " +
            "INNER JOIN exercises e ON le.exerciseId = e.id " +
            "INNER JOIN workout_sessions w ON le.sessionId = w.id " +
            "WHERE w.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay"
    )
    fun observeSetsWithMusclesInRange(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<SetWithMuscles>>

    // --- writes ------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedExercise(loggedExercise: LoggedExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: LoggedSet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<LoggedSet>): List<Long>

    /**
     * Atomically persists a whole workout: inserts [session], then for each
     * [NewLoggedExercise] inserts the logged exercise stamped with the new
     * session id, then its sets stamped with the new logged-exercise id. The
     * `@Transaction` on this Kotlin default interface method makes the entire
     * graph commit or roll back as one unit, so a mid-way failure never leaves a
     * partial session behind. Returns the new session id.
     */
    @Transaction
    suspend fun saveSessionGraph(
        session: WorkoutSession,
        exercises: List<NewLoggedExercise>,
    ): Long {
        val sessionId = insertSession(session)
        exercises.forEach { ex ->
            val loggedExerciseId = insertLoggedExercise(
                LoggedExercise(
                    sessionId = sessionId,
                    exerciseId = ex.exerciseId,
                    order = ex.order,
                ),
            )
            if (ex.sets.isNotEmpty()) {
                insertSets(
                    ex.sets.mapIndexed { index, set ->
                        LoggedSet(
                            loggedExerciseId = loggedExerciseId,
                            setNumber = index + 1,
                            reps = set.reps,
                            weightKg = set.weightKg,
                            rpe = set.rpe,
                            isDone = set.isDone,
                            isWarmup = set.isWarmup,
                        )
                    },
                )
            }
        }
        return sessionId
    }

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Update
    suspend fun updateLoggedExercise(loggedExercise: LoggedExercise)

    @Update
    suspend fun updateSet(set: LoggedSet)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Delete
    suspend fun deleteLoggedExercise(loggedExercise: LoggedExercise)

    @Delete
    suspend fun deleteSet(set: LoggedSet)
}
