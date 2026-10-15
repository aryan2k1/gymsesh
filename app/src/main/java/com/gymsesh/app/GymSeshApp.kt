package com.gymsesh.app

import android.app.Application
import android.util.Log
import com.gymsesh.app.di.SeedEntryPoint
import com.gymsesh.app.di.SplitEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class GymSeshApp : Application() {

    // Application-scoped background scope for one-off startup work (e.g. seeding).
    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO +
            CoroutineExceptionHandler { _, t -> Log.e(TAG, "Uncaught startup error", t) },
    )

    override fun onCreate() {
        super.onCreate()
        seedCatalogueThenSplits()
    }

    /**
     * Kicks off first-run seeding off the main thread; never crashes the app.
     * Splits depend on exercise ids, so they are seeded *after* exercises
     * complete, chained in the same coroutine.
     */
    private fun seedCatalogueThenSplits() {
        appScope.launch {
            try {
                val exerciseSeeder = EntryPointAccessors
                    .fromApplication(this@GymSeshApp, SeedEntryPoint::class.java)
                    .exerciseSeeder()
                exerciseSeeder.seedIfEmpty()

                val splitSeeder = EntryPointAccessors
                    .fromApplication(this@GymSeshApp, SplitEntryPoint::class.java)
                    .splitSeeder()
                splitSeeder.seedIfEmpty()
            } catch (t: Throwable) {
                Log.e(TAG, "Startup seeding failed", t)
            }
        }
    }

    companion object {
        private const val TAG = "GymSeshApp"
    }
}
