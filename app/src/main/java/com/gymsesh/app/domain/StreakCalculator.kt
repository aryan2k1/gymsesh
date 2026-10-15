package com.gymsesh.app.domain

/**
 * Pure "gym streak" math over the set of epoch-days (LocalDate.toEpochDay())
 * that have at least one logged workout. No Android/Room dependencies so it can
 * be used from ViewModels, the Glance widget, and previews alike.
 */
object StreakCalculator {

    /**
     * "Don't break the chain" streak: the number of consecutive calendar days
     * with a workout, counting back from the most recent workout day.
     *
     * Not having trained *today* does NOT reset the streak — it stays alive as
     * long as the last workout was today or yesterday, so the number doesn't
     * drop to 0 just because it's early in the day.
     *
     * @param workoutDays distinct epoch-days that have >= 1 workout
     * @param todayEpoch  today's epoch-day (LocalDate.now().toEpochDay())
     * @return consecutive workout-day count ending at the most recent workout,
     *         or 0 if the last workout was before yesterday / there are none.
     */
    fun currentStreak(workoutDays: Set<Long>, todayEpoch: Long): Int {
        if (workoutDays.isEmpty()) return 0
        val mostRecent = workoutDays.max()
        // Streak is only "alive" if the last session was today or yesterday.
        if (mostRecent < todayEpoch - 1) return 0
        var day = mostRecent
        var count = 0
        while (day in workoutDays) {
            count++
            day--
        }
        return count
    }
}
