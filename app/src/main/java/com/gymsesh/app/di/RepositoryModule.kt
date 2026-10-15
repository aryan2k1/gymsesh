package com.gymsesh.app.di

import com.gymsesh.app.data.repository.ExerciseRepository
import com.gymsesh.app.data.repository.ExerciseRepositoryImpl
import com.gymsesh.app.data.repository.SplitRepository
import com.gymsesh.app.data.repository.SplitRepositoryImpl
import com.gymsesh.app.data.repository.WorkoutRepository
import com.gymsesh.app.data.repository.WorkoutRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindSplitRepository(impl: SplitRepositoryImpl): SplitRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository
}
