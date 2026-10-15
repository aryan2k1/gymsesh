package com.gymsesh.app.data.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.gymsesh.app.data.db.entities.SplitTemplate
import com.gymsesh.app.data.db.entities.TemplateDay
import com.gymsesh.app.data.db.entities.TemplateExercise

/** A [TemplateDay] together with its ordered exercise slots. */
data class TemplateDayWithExercises(
    @Embedded val day: TemplateDay,
    @Relation(
        parentColumn = "id",
        entityColumn = "dayId",
    )
    val exercises: List<TemplateExercise>,
)

/** A full [SplitTemplate] with all of its days (each with their exercises). */
data class TemplateWithDays(
    @Embedded val template: SplitTemplate,
    @Relation(
        entity = TemplateDay::class,
        parentColumn = "id",
        entityColumn = "templateId",
    )
    val days: List<TemplateDayWithExercises>,
)
