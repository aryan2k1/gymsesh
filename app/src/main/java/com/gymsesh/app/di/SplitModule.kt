package com.gymsesh.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gymsesh.app.data.prefs.SplitPreferences
import com.gymsesh.app.data.seed.SplitSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides split-related singletons: [SplitPreferences] (backed by the shared
 * settings DataStore from `PrefsModule`).
 */
@Module
@InstallIn(SingletonComponent::class)
object SplitModule {

    @Provides
    @Singleton
    fun provideSplitPreferences(
        dataStore: DataStore<Preferences>,
    ): SplitPreferences = SplitPreferences(dataStore)
}

/**
 * Hilt entry point for reaching the [SplitSeeder] from `GymSeshApp`.
 *
 * [SplitSeeder] is constructor-injected (`@Inject` + `@Singleton`); the
 * `@HiltAndroidApp` Application pulls it out of the singleton graph via this
 * entry point in `onCreate`, mirroring [SeedEntryPoint].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SplitEntryPoint {
    fun splitSeeder(): SplitSeeder
}
