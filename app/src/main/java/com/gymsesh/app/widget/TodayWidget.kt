package com.gymsesh.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.gymsesh.app.MainActivity
import com.gymsesh.app.R
import com.gymsesh.app.data.repository.WorkoutRepository
import com.gymsesh.app.domain.StreakCalculator
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Immutable snapshot of "today" resolved off the DB before rendering.
 * Rendering is pure and reads only this state — no suspend work inside the
 * GlanceComposable tree.
 */
private data class TodayState(
    val loggedToday: Boolean,
    val exerciseCount: Int,
    val streak: Int = 0,
    val failed: Boolean = false,
)

/**
 * Home-screen "Today" widget. Shows whether a workout has been logged today and,
 * if so, how many exercises. Tapping anywhere opens [MainActivity].
 *
 * Data access is intentionally simple and self-contained: it reads the repository
 * out of the Hilt singleton graph via [WidgetEntryPoint] (a GlanceAppWidget cannot
 * be `@Inject`-ed) and takes the first emission of the relevant flows. Every read
 * is guarded so the widget can never crash the launcher — on any failure it shows a
 * safe fallback.
 */
class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadTodayState(context)
        provideContent {
            GlanceTheme {
                TodayContent(context, state)
            }
        }
    }

    /** Resolves today's status. Never throws — returns a failed state instead. */
    private suspend fun loadTodayState(context: Context): TodayState = try {
        val repo: WorkoutRepository = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .workoutRepository()

        val today = LocalDate.now().toEpochDay()
        val workoutDays = repo.observeSessions().first()
            .map { it.dateEpochDay }
            .toSet()
        val streak = StreakCalculator.currentStreak(workoutDays, today)
        val sessionsToday = repo.observeSessionsInRange(today, today).first()
        if (sessionsToday.isEmpty()) {
            TodayState(loggedToday = false, exerciseCount = 0, streak = streak)
        } else {
            val exerciseCount = repo.observeSessionsWithExercises().first()
                .filter { it.session.dateEpochDay == today }
                .sumOf { it.exercises.size }
            TodayState(loggedToday = true, exerciseCount = exerciseCount, streak = streak)
        }
    } catch (t: Throwable) {
        TodayState(loggedToday = false, exerciseCount = 0, streak = 0, failed = true)
    }
}

private fun summaryText(context: Context, state: TodayState): String = when {
    state.failed -> context.getString(R.string.widget_unavailable)
    state.loggedToday -> context.getString(R.string.widget_logged, state.exerciseCount)
    else -> context.getString(R.string.widget_none)
}

private fun streakText(context: Context, state: TodayState): String =
    if (state.streak > 0) {
        context.getString(R.string.widget_streak, state.streak)
    } else {
        context.getString(R.string.widget_streak_none)
    }

@Composable
private fun TodayContent(context: Context, state: TodayState) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = context.getString(R.string.widget_title),
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = summaryText(context, state),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = streakText(context, state),
            style = TextStyle(
                color = GlanceTheme.colors.secondary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
        )
    }
}
