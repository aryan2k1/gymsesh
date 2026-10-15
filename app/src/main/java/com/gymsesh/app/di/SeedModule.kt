package com.gymsesh.app.di

import com.gymsesh.app.data.seed.ExerciseSeeder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for reaching the [ExerciseSeeder] from `GymSeshApp`.
 *
 * [ExerciseSeeder] is constructor-injected (`@Inject` + `@Singleton`), so it
 * needs no `@Provides`/`@Binds`. The `@HiltAndroidApp` Application cannot use
 * field injection into itself, so we pull the seeder out of the singleton graph
 * via this entry point in `onCreate`.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SeedEntryPoint {
    fun exerciseSeeder(): ExerciseSeeder
}
