package com.gymsesh.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single set of a [LoggedExercise]: reps × weight, with optional RPE and
 * done/warm-up flags. Deleting the parent logged exercise cascades.
 */
@Entity(
    tableName = "logged_sets",
    foreignKeys = [
        ForeignKey(
            entity = LoggedExercise::class,
            parentColumns = ["id"],
            childColumns = ["loggedExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("loggedExerciseId")],
)
data class LoggedSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loggedExerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val rpe: Double? = null,
    val isDone: Boolean = false,
    val isWarmup: Boolean = false,
)
