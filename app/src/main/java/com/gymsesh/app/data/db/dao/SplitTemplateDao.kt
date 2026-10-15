package com.gymsesh.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymsesh.app.data.db.entities.SplitTemplate
import com.gymsesh.app.data.db.entities.TemplateDay
import com.gymsesh.app.data.db.entities.TemplateExercise
import com.gymsesh.app.data.db.relations.TemplateWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitTemplateDao {

    @Query("SELECT * FROM split_templates ORDER BY name COLLATE NOCASE ASC")
    fun observeTemplates(): Flow<List<SplitTemplate>>

    @Transaction
    @Query("SELECT * FROM split_templates ORDER BY name COLLATE NOCASE ASC")
    fun observeTemplatesWithDays(): Flow<List<TemplateWithDays>>

    @Transaction
    @Query("SELECT * FROM split_templates WHERE id = :templateId")
    fun observeTemplateWithDays(templateId: Long): Flow<TemplateWithDays?>

    @Query("SELECT * FROM template_days WHERE templateId = :templateId ORDER BY position ASC")
    fun observeDays(templateId: Long): Flow<List<TemplateDay>>

    @Query("SELECT * FROM template_exercises WHERE dayId = :dayId ORDER BY id ASC")
    fun observeExercises(dayId: Long): Flow<List<TemplateExercise>>

    // --- writes ------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: SplitTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: TemplateDay): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<TemplateDay>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: TemplateExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<TemplateExercise>): List<Long>

    @Update
    suspend fun updateTemplate(template: SplitTemplate)

    @Update
    suspend fun updateDay(day: TemplateDay)

    @Update
    suspend fun updateExercise(exercise: TemplateExercise)

    @Delete
    suspend fun deleteTemplate(template: SplitTemplate)

    @Delete
    suspend fun deleteDay(day: TemplateDay)

    @Delete
    suspend fun deleteExercise(exercise: TemplateExercise)
}
