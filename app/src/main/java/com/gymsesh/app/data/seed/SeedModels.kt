package com.gymsesh.app.data.seed

import kotlinx.serialization.Serializable

/**
 * Root of the bundled `assets/seed/exercises.json` snapshot.
 *
 * Parsed on first run by [ExerciseSeeder] and mapped into Room `Exercise` rows.
 */
@Serializable
data class SeedFile(
    val version: Int = 1,
    val exercises: List<SeedExercise> = emptyList(),
)

/**
 * A single curated exercise as stored in the seed JSON. String muscle/equipment
 * fields are the *names* of the [com.gymsesh.app.data.model.MuscleGroup] /
 * [com.gymsesh.app.data.model.Equipment] enums; the seeder maps them via a safe
 * `valueOf` with fallback so an unknown value never crashes the import.
 */
@Serializable
data class SeedExercise(
    val name: String,
    val primaryMuscle: String,
    val secondaryMuscles: List<String> = emptyList(),
    val equipment: String,
    val instructions: String? = null,
    val imagePath: String? = null,
)
