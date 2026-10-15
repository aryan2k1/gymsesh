package com.gymsesh.app.di

import android.content.Context
import androidx.room.Room
import com.gymsesh.app.data.db.GymSeshDatabase
import com.gymsesh.app.data.db.dao.ExerciseDao
import com.gymsesh.app.data.db.dao.SplitTemplateDao
import com.gymsesh.app.data.db.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): GymSeshDatabase =
        Room.databaseBuilder(
            context,
            GymSeshDatabase::class.java,
            GymSeshDatabase.DATABASE_NAME,
        )
            // FK constraints are declared on entities; ensure they're enforced.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideExerciseDao(db: GymSeshDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideSplitTemplateDao(db: GymSeshDatabase): SplitTemplateDao = db.splitTemplateDao()

    @Provides
    fun provideWorkoutDao(db: GymSeshDatabase): WorkoutDao = db.workoutDao()
}
