package com.gymsesh.app.data.model

/**
 * Lightweight projection of a single logged set together with the muscle groups
 * of the exercise it belongs to. This is the raw feed for the muscle-group pie
 * chart: aggregate SETS per [MuscleGroup] (primary = full weight, secondary =
 * half weight) over a date window. Warm-up flag is exposed so callers can decide
 * whether to include warm-up sets.
 */
data class SetWithMuscles(
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup>,
    val isWarmup: Boolean,
)
