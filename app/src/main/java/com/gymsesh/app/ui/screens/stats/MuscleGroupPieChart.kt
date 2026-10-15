package com.gymsesh.app.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymsesh.app.R
import com.gymsesh.app.data.model.MuscleGroup
import java.util.Locale

/**
 * Fixed, legible categorical colour for each of the 12 [MuscleGroup]s. This is the one
 * place GymSesh hardcodes colour (BUILD_BIBLE §5 allows the pie's categorical palette as
 * the explicit exception): distinct mid-tone hues chosen to stay readable as filled arcs
 * on both the light and dark chart surfaces. Keep this map stable so a muscle always reads
 * as the same colour across sessions.
 */
fun muscleColor(muscle: MuscleGroup): Color = when (muscle) {
    MuscleGroup.CHEST -> Color(0xFFE34948) // red
    MuscleGroup.BACK -> Color(0xFF2A78D6) // blue
    MuscleGroup.SHOULDERS -> Color(0xFFEB6834) // orange
    MuscleGroup.BICEPS -> Color(0xFF6A4CC2) // violet
    MuscleGroup.TRICEPS -> Color(0xFF1BAF7A) // aqua
    MuscleGroup.FOREARMS -> Color(0xFF8D6E63) // brown
    MuscleGroup.QUADS -> Color(0xFF2E9E4F) // green
    MuscleGroup.HAMSTRINGS -> Color(0xFF0E9EC4) // cyan
    MuscleGroup.GLUTES -> Color(0xFFE87BA4) // pink
    MuscleGroup.CALVES -> Color(0xFFE0A100) // amber
    MuscleGroup.ABS -> Color(0xFF7CB342) // lime
    MuscleGroup.TRAPS -> Color(0xFF5C6BC0) // indigo
}

/**
 * A custom Canvas donut chart of the muscle-group split, with a legend beneath it.
 * Each arc is sized by [MuscleSlice.fraction]; the legend pairs a colour swatch with the
 * muscle name and its percentage. When [slices] is empty a friendly prompt is shown instead.
 */
@Composable
fun MuscleGroupPieChart(
    slices: List<MuscleSlice>,
    modifier: Modifier = Modifier,
) {
    if (slices.isEmpty()) {
        PieEmptyState(modifier = modifier)
        return
    }

    val ringColor = MaterialTheme.colorScheme.surface
    val totalWeighted = slices.sumOf { it.weightedSets }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
            ) {
                val side = minOf(size.width, size.height)
                val strokeWidth = side * 0.20f
                val diameter = side - strokeWidth
                val topLeft = Offset(
                    x = (size.width - diameter) / 2f,
                    y = (size.height - diameter) / 2f,
                )
                val arcSize = Size(diameter, diameter)
                // A small surface-coloured gap between arcs keeps neighbours distinct.
                val gapDegrees = if (slices.size > 1) 2f else 0f
                var startAngle = -90f
                slices.forEach { slice ->
                    val full = slice.fraction * 360f
                    val sweep = (full - gapDegrees).coerceAtLeast(0f)
                    drawArc(
                        color = muscleColor(slice.muscle),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                    startAngle += full
                }
                // Thin surface ring on the inner edge to crisp up the donut hole.
                drawCircle(
                    color = ringColor,
                    radius = (diameter - strokeWidth) / 2f,
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatSets(totalWeighted),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.stats_pie_center_sets),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.padding(top = 8.dp))

        slices.forEach { slice -> LegendRow(slice) }
    }
}

@Composable
private fun LegendRow(slice: MuscleSlice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(muscleColor(slice.muscle)),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = slice.muscle.displayLabel(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatPercent(slice.fraction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PieEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.stats_pie_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.padding(top = 4.dp))
        Text(
            text = stringResource(R.string.stats_pie_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** e.g. `0.242f` -> "24%". */
private fun formatPercent(fraction: Float): String =
    String.format(Locale.US, "%d%%", Math.round(fraction * 100f))

/** Weighted set counts may be fractional; show a whole number when it is one. */
private fun formatSets(value: Double): String =
    if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }

/** Local human-readable label (e.g. `HAMSTRINGS` -> "Hamstrings") to keep stats self-contained. */
private fun MuscleGroup.displayLabel(): String =
    name.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }
