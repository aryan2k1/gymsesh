package com.gymsesh.app.data.repository

/**
 * Plain (id-less) description of a workout to persist atomically.
 *
 * These holders let callers (e.g. the log flow) describe the session → exercise
 * → set graph without pre-assigning Room primary keys: the transactional save
 * inserts the parent, then stamps the returned id onto its children. See
 * [WorkoutRepository.saveSession] / [com.gymsesh.app.data.db.dao.WorkoutDao.saveSessionGraph].
 */
data class NewLoggedExercise(
    val exerciseId: Long,
    val order: Int,
    val sets: List<NewSet>,
)

/** One set to insert; [setNumber] is derived from ordering at save time. */
data class NewSet(
    val reps: Int,
    val weightKg: Double,
    val rpe: Double? = null,
    val isWarmup: Boolean = false,
    val isDone: Boolean = false,
)
