package com.gymsesh.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymsesh.app.data.model.SplitType

/** A workout split (e.g. PPL). Parent of [TemplateDay]. */
@Entity(tableName = "split_templates")
data class SplitTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: SplitType,
)
