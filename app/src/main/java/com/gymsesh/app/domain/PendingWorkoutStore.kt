package com.gymsesh.app.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory bridge that carries a "start this split day" request from the Today
 * tab to the Log tab without threading it through navigation arguments.
 *
 * The Today screen [request]s a [com.gymsesh.app.data.db.entities.TemplateDay]
 * id just before navigating to Log; the Log screen [consume]s it once on entry
 * to pre-fill a session. Process-scoped singleton (see Hilt): a single pending
 * id at a time, cleared on consume, so a stale request never fires twice.
 */
@Singleton
class PendingWorkoutStore @Inject constructor() {

    @Volatile
    private var pendingTemplateDayId: Long? = null

    /** Records a template-day to start when Log next opens. */
    fun request(templateDayId: Long) {
        pendingTemplateDayId = templateDayId
    }

    /** Returns the pending template-day id (if any) and clears it. */
    fun consume(): Long? {
        val id = pendingTemplateDayId
        pendingTemplateDayId = null
        return id
    }
}
