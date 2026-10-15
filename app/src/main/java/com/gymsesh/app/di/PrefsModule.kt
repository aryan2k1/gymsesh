package com.gymsesh.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.gymsesh.app.data.prefs.ThemePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Single Preferences DataStore instance for the whole app.
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gymsesh_settings",
)

/** Provides DataStore-backed preference stores as singletons. */
@Module
@InstallIn(SingletonComponent::class)
object PrefsModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.settingsDataStore

    @Provides
    @Singleton
    fun provideThemePreferences(
        dataStore: DataStore<Preferences>,
    ): ThemePreferences = ThemePreferences(dataStore)
}
