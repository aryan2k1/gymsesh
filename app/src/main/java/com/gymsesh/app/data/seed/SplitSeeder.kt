package com.gymsesh.app.data.seed

import android.content.Context
import android.util.Log
import com.gymsesh.app.data.db.entities.SplitTemplate
import com.gymsesh.app.data.db.entities.TemplateDay
import com.gymsesh.app.data.db.entities.TemplateExercise
import com.gymsesh.app.data.model.SplitType
import com.gymsesh.app.data.repository.ExerciseRepository
import com.gymsesh.app.data.repository.SplitRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * First-run importer for the prebuilt split templates.
 *
 * Reads the bundled `assets/seed/splits.json` snapshot and inserts each
 * template as `SplitTemplate` → `TemplateDay`(order = index) →
 * `TemplateExercise`, resolving each exercise *name* to its catalogue id via
 * [ExerciseRepository]. Idempotent: it only seeds when `split_templates` is
 * empty. MUST run *after* [ExerciseSeeder] because it depends on exercise ids
 * existing. Runs entirely on [Dispatchers.IO] and never crashes the app.
 */
@Singleton
class SplitSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val splitRepository: SplitRepository,
    private val exerciseRepository: ExerciseRepository,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Seeds the split templates if none exist yet. Safe to call on every app
     * start; returns the number of templates inserted (0 if skipped or nothing
     * valid was parsed).
     */
    suspend fun seedIfEmpty(): Int = withContext(Dispatchers.IO) {
        try {
            if (splitRepository.observeTemplates().first().isNotEmpty()) {
                Log.d(TAG, "Split templates already populated; skipping seed.")
                return@withContext 0
            }

            val raw = try {
                context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            } catch (t: Throwable) {
                Log.e(TAG, "Could not read seed asset '$ASSET_PATH'", t)
                return@withContext 0
            }

            val seed = try {
                json.decodeFromString<SplitSeedFile>(raw)
            } catch (t: Throwable) {
                Log.e(TAG, "Could not parse seed asset '$ASSET_PATH'", t)
                return@withContext 0
            }

            if (seed.templates.isEmpty()) {
                Log.w(TAG, "Seed file contained no templates.")
                return@withContext 0
            }

            // Resolve exercise names → ids once. Catalogue names are unique.
            val idByName = exerciseRepository.observeExercises().first()
                .associate { it.name.trim().lowercase() to it.id }
            if (idByName.isEmpty()) {
                Log.w(TAG, "Exercise catalogue empty; cannot resolve split exercises. Skipping.")
                return@withContext 0
            }

            // Guard against a race between the empty-check and insert.
            if (splitRepository.observeTemplates().first().isNotEmpty()) {
                Log.d(TAG, "Templates populated concurrently; skipping insert.")
                return@withContext 0
            }

            var inserted = 0
            for (template in seed.templates) {
                val type = typeOrNull(template.type)
                if (type == null) {
                    Log.w(TAG, "Skipping template '${template.name}': unknown type '${template.type}'.")
                    continue
                }
                val templateId = splitRepository.insertTemplate(
                    SplitTemplate(name = template.name, type = type),
                )
                template.days.forEachIndexed { dayIndex, day ->
                    val dayId = splitRepository.insertDay(
                        TemplateDay(
                            templateId = templateId,
                            dayName = day.dayName,
                            order = dayIndex,
                        ),
                    )
                    val slots = day.exercises.mapNotNull { slot ->
                        val exerciseId = idByName[slot.name.trim().lowercase()]
                        if (exerciseId == null) {
                            Log.w(
                                TAG,
                                "Skipping unknown exercise '${slot.name}' in " +
                                    "'${template.name}' / '${day.dayName}'.",
                            )
                            null
                        } else {
                            TemplateExercise(
                                dayId = dayId,
                                exerciseId = exerciseId,
                                targetSets = slot.targetSets,
                                targetReps = slot.targetReps,
                            )
                        }
                    }
                    if (slots.isNotEmpty()) splitRepository.insertExercises(slots)
                }
                inserted++
            }

            Log.i(TAG, "Seeded $inserted split templates.")
            inserted
        } catch (t: Throwable) {
            Log.e(TAG, "Split seeding failed", t)
            0
        }
    }

    private fun typeOrNull(value: String): SplitType? =
        runCatching { SplitType.valueOf(value.trim().uppercase()) }.getOrNull()

    companion object {
        private const val TAG = "SplitSeeder"
        private const val ASSET_PATH = "seed/splits.json"
    }
}
