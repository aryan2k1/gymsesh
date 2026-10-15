package com.gymsesh.app.data.seed

import kotlinx.serialization.Serializable

/**
 * Root of the bundled `assets/seed/splits.json` snapshot.
 *
 * Parsed on first run by [SplitSeeder] and mapped into Room `SplitTemplate` →
 * `TemplateDay` → `TemplateExercise` rows. Exercises are referenced by *name*
 * and resolved to catalogue ids at import time.
 */
@Serializable
data class SplitSeedFile(
    val version: Int = 1,
    val templates: List<SplitSeedTemplate> = emptyList(),
)

/**
 * A single prebuilt split. [type] is the *name* of a
 * [com.gymsesh.app.data.model.SplitType] enum value; the seeder maps it via a
 * safe `valueOf` so an unknown value never crashes the import.
 */
@Serializable
data class SplitSeedTemplate(
    val name: String,
    val type: String,
    val days: List<SplitSeedDay> = emptyList(),
)

/** One ordered day within a [SplitSeedTemplate] (e.g. "Push"). */
@Serializable
data class SplitSeedDay(
    val dayName: String,
    val exercises: List<SplitSeedExercise> = emptyList(),
)

/**
 * An exercise slot inside a [SplitSeedDay]. [name] must match an exercise name
 * in `assets/seed/exercises.json`; unknown names are skipped (and logged) by the
 * seeder rather than failing the whole import.
 */
@Serializable
data class SplitSeedExercise(
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
)
