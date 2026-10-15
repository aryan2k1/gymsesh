package com.gymsesh.app.data.seed

import android.content.Context
import android.util.Log
import com.gymsesh.app.data.db.entities.Exercise
import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup
import com.gymsesh.app.data.repository.ExerciseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * First-run importer for the curated core exercise catalogue.
 *
 * Reads the bundled `assets/seed/exercises.json` snapshot, maps each DTO to a
 * Room [Exercise], and bulk-inserts. Idempotent: it only seeds when the
 * `exercises` table is empty, so it never double-seeds across launches.
 */
@Singleton
class ExerciseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ExerciseRepository,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Seeds the exercise catalogue if it is empty. Safe to call on every app
     * start; runs entirely on [Dispatchers.IO]. Returns the number of exercises
     * inserted (0 if seeding was skipped or nothing valid was parsed).
     */
    suspend fun seedIfEmpty(): Int = withContext(Dispatchers.IO) {
        if (repository.count() > 0) {
            Log.d(TAG, "Exercise catalogue already populated; skipping seed.")
            return@withContext 0
        }

        val raw = try {
            context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        } catch (t: Throwable) {
            Log.e(TAG, "Could not read seed asset '$ASSET_PATH'", t)
            return@withContext 0
        }

        val seed = try {
            json.decodeFromString<SeedFile>(raw)
        } catch (t: Throwable) {
            Log.e(TAG, "Could not parse seed asset '$ASSET_PATH'", t)
            return@withContext 0
        }

        val exercises = seed.exercises.mapNotNull { it.toEntityOrNull() }
        if (exercises.isEmpty()) {
            Log.w(TAG, "Seed file contained no importable exercises.")
            return@withContext 0
        }

        // Guard against a race between the empty-check and insert (e.g. two
        // launches overlapping): re-check inside IO before writing.
        if (repository.count() > 0) {
            Log.d(TAG, "Catalogue populated concurrently; skipping insert.")
            return@withContext 0
        }

        repository.insertAll(exercises)
        Log.i(TAG, "Seeded ${exercises.size} exercises into the catalogue.")
        exercises.size
    }

    /**
     * Maps a [SeedExercise] to an [Exercise], returning null (with a log) when
     * the primary muscle or equipment name is unknown. Unknown secondary
     * muscles are dropped individually rather than failing the whole row.
     */
    private fun SeedExercise.toEntityOrNull(): Exercise? {
        val primary = muscleOrNull(primaryMuscle)
        if (primary == null) {
            Log.w(TAG, "Skipping '$name': unknown primaryMuscle '$primaryMuscle'.")
            return null
        }
        val equip = equipmentOrNull(equipment)
        if (equip == null) {
            Log.w(TAG, "Skipping '$name': unknown equipment '$equipment'.")
            return null
        }
        val secondary = secondaryMuscles
            .mapNotNull { name ->
                muscleOrNull(name).also {
                    if (it == null) Log.w(TAG, "Dropping unknown secondary muscle '$name' on '${this.name}'.")
                }
            }
            .filter { it != primary }
            .distinct()

        return Exercise(
            name = name,
            primaryMuscle = primary,
            secondaryMuscles = secondary,
            equipment = equip,
            imagePath = imagePath,
            instructions = instructions,
            isCustom = false,
        )
    }

    private fun muscleOrNull(value: String): MuscleGroup? =
        runCatching { MuscleGroup.valueOf(value.trim().uppercase()) }.getOrNull()

    private fun equipmentOrNull(value: String): Equipment? =
        runCatching { Equipment.valueOf(value.trim().uppercase()) }.getOrNull()

    companion object {
        private const val TAG = "ExerciseSeeder"
        private const val ASSET_PATH = "seed/exercises.json"
    }
}
