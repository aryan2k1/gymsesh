package com.gymsesh.app.ui.screens.library

import com.gymsesh.app.data.model.Equipment
import com.gymsesh.app.data.model.MuscleGroup
import java.util.Locale

/**
 * Human-readable labels derived from the data enums (e.g. `SHOULDERS` -> "Shoulders",
 * `UPPER_LOWER`-style names -> "Upper Lower"). Derived from the enum name rather than
 * hardcoded copy, so it stays in sync automatically as the enums grow. If localisation
 * of these labels is ever needed they can move to string resources.
 */
internal fun MuscleGroup.label(): String = name.toDisplayLabel()

internal fun Equipment.label(): String = name.toDisplayLabel()

private fun String.toDisplayLabel(): String =
    split('_').joinToString(" ") { word ->
        word.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }
    }
