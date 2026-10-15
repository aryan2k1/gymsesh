package com.gymsesh.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed store for the user's active split selection.
 *
 * Persists the active [com.gymsesh.app.data.db.entities.SplitTemplate] id
 * (nullable) plus a rotation index — which day of that split counts as "today".
 * Provided as a singleton via Hilt (see `di/SplitModule.kt`), reusing the shared
 * settings DataStore. Mirrors [ThemePreferences].
 */
class SplitPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val ACTIVE_TEMPLATE = longPreferencesKey("split_active_template_id")
        val ROTATION_INDEX = intPreferencesKey("split_rotation_index")
    }

    // A sentinel marks "no active split" since Preferences has no nullable key.
    private val noTemplate = Long.MIN_VALUE

    /** The active split template id, or `null` when none is chosen. */
    val activeTemplateId: Flow<Long?> = dataStore.data.map { prefs ->
        val id = prefs[Keys.ACTIVE_TEMPLATE] ?: noTemplate
        if (id == noTemplate) null else id
    }

    /** Which day of the active split is "today" (0-based, unbounded here). */
    val rotationIndex: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.ROTATION_INDEX] ?: 0
    }

    /**
     * Sets (or clears with `null`) the active split. Changing the split resets
     * the rotation to the first day.
     */
    suspend fun setActiveTemplate(id: Long?) {
        dataStore.edit { prefs ->
            if (id == null) prefs.remove(Keys.ACTIVE_TEMPLATE) else prefs[Keys.ACTIVE_TEMPLATE] = id
            prefs[Keys.ROTATION_INDEX] = 0
        }
    }

    /** Sets the rotation day index directly. */
    suspend fun setRotationIndex(index: Int) {
        dataStore.edit { it[Keys.ROTATION_INDEX] = index }
    }
}
