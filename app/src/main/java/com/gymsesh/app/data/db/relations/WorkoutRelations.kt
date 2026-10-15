package com.gymsesh.app.data.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.gymsesh.app.data.db.entities.LoggedExercise
import com.gymsesh.app.data.db.entities.LoggedSet
import com.gymsesh.app.data.db.entities.WorkoutSession

/** A [LoggedExercise] together with all of its logged sets. */
data class LoggedExerciseWithSets(
    @Embedded val loggedExercise: LoggedExercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "loggedExerciseId",
    )
    val sets: List<LoggedSet>,
)

/** A full [WorkoutSession] with its ordered exercises (each with their sets). */
data class SessionWithExercises(
    @Embedded val session: WorkoutSession,
    @Relation(
        entity = LoggedExercise::class,
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val exercises: List<LoggedExerciseWithSets>,
)
