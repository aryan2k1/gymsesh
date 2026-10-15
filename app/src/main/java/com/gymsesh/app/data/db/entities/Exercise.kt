package com.gymsesh.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup

/**
 * A single exercise in the catalogue. Seeded from the wger dataset, plus any
 * user-created custom exercises ([isCustom] = true).
 */
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: Equipment,
    val imagePath: String? = null,
    val instructions: String? = null,
    val isCustom: Boolean = false,
)
