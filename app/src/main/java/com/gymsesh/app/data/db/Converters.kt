package com.gymsesh.app.data.db

import androidx.room.TypeConverter
import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.data.model.SplitType

/**
 * Room type converters. Enums are persisted as their [Enum.name] string, and
 * `List<MuscleGroup>` as a comma-delimited string of enum names (empty string
 * for an empty list). Using enum names keeps the DB human-readable and stable
 * against ordinal reordering.
 */
class Converters {

    // --- List<MuscleGroup> -------------------------------------------------

    @TypeConverter
    fun muscleListToString(muscles: List<MuscleGroup>?): String =
        muscles?.joinToString(DELIMITER) { it.name }.orEmpty()

    @TypeConverter
    fun stringToMuscleList(value: String?): List<MuscleGroup> =
        if (value.isNullOrEmpty()) {
            emptyList()
        } else {
            value.split(DELIMITER).map { MuscleGroup.valueOf(it) }
        }

    // --- MuscleGroup -------------------------------------------------------

    @TypeConverter
    fun muscleGroupToString(value: MuscleGroup?): String? = value?.name

    @TypeConverter
    fun stringToMuscleGroup(value: String?): MuscleGroup? =
        value?.let { MuscleGroup.valueOf(it) }

    // --- Equipment ---------------------------------------------------------

    @TypeConverter
    fun equipmentToString(value: Equipment?): String? = value?.name

    @TypeConverter
    fun stringToEquipment(value: String?): Equipment? =
        value?.let { Equipment.valueOf(it) }

    // --- SplitType ---------------------------------------------------------

    @TypeConverter
    fun splitTypeToString(value: SplitType?): String? = value?.name

    @TypeConverter
    fun stringToSplitType(value: String?): SplitType? =
        value?.let { SplitType.valueOf(it) }

    private companion object {
        const val DELIMITER = ","
    }
}
