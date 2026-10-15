package com.gymsesh.app.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single day within a [SplitTemplate] (e.g. "Push"). Parent of
 * [TemplateExercise]. Deleting a template cascades to its days.
 */
@Entity(
    tableName = "template_days",
    foreignKeys = [
        ForeignKey(
            entity = SplitTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("templateId")],
)
data class TemplateDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val dayName: String,
    // `order` is a SQL keyword; stored as `position`, exposed as `order`.
    @ColumnInfo(name = "position") val order: Int,
)
