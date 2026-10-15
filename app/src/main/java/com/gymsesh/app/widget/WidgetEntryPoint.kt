package com.gymsesh.app.widget

import com.gymsesh.app.data.repository.WorkoutRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for reaching repositories from the Glance widget.
 *
 * A [androidx.glance.appwidget.GlanceAppWidget] is instantiated by the framework
 * and cannot use `@Inject`/field injection, so — mirroring
 * [com.gymsesh.app.di.SeedEntryPoint] — we pull the constructor-injected
 * [WorkoutRepository] singleton out of the graph via
 * `EntryPointAccessors.fromApplication(context.applicationContext, ...)`.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun workoutRepository(): WorkoutRepository
}
