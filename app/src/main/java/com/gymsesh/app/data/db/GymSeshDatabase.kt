package com.gymsesh.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymsesh.app.data.db.dao.ExerciseDao
import com.gymsesh.app.data.db.dao.SplitTemplateDao
import com.gymsesh.app.data.db.dao.WorkoutDao
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.db.entities.LoggedExercise
import com.gymsesh.app.data.db.entities.LoggedSet
import com.gymsesh.app.data.db.entities.SplitTemplate
import com.gymsesh.app.data.db.entities.TemplateDay
import com.gymsesh.app.data.db.entities.TemplateExercise
import com.gymsesh.app.data.db.entities.WorkoutSession

@Database(
    entities = [
        Exercise::class,
        SplitTemplate::class,
        TemplateDay::class,
        TemplateExercise::class,
        WorkoutSession::class,
        LoggedExercise::class,
        LoggedSet::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GymSeshDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun splitTemplateDao(): SplitTemplateDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        const val DATABASE_NAME = "gymsesh.db"
    }
}
