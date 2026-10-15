package com.gymsesh.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A logged workout on a given day. Optionally derived from a [TemplateDay].
 * Parent of [LoggedExercise]. If the source template day is deleted the link is
 * nulled (SET_NULL) rather than deleting the session's history.
 */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TemplateDay::class,
            parentColumns = ["id"],
            childColumns = ["templateDayId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("templateDayId")],
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val templateDayId: Long? = null,
    val notes: String? = null,
    val durationSec: Int? = null,
)
