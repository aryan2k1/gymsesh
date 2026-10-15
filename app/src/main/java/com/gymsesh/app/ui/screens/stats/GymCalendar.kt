package com.gymsesh.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gymsesh.app.R
import com.gymsesh.app.ui.theme.GymSeshTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * A dependency-free month calendar. Renders a 7-column grid (Mon–Sun) for one month and
 * highlights every day that has at least one logged workout with a filled
 * [MaterialTheme.colorScheme] `primary` circle; today is outlined. Prev/next arrows page
 * between months. The displayed month is internal state, seeded from [initialMonth]; the
 * highlight/outline logic is pure so it renders identically in previews.
 *
 * @param workoutDays distinct epoch-days (LocalDate.toEpochDay()) with >= 1 workout.
 * @param today the day to outline as "today".
 * @param initialMonth the month shown first (defaults to [today]'s month).
 */
@Composable
fun GymCalendar(
    workoutDays: Set<Long>,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    initialMonth: YearMonth = YearMonth.from(today),
) {
    var month by remember { mutableStateOf(initialMonth) }

    Column(modifier = modifier.fillMaxWidth()) {
        MonthHeader(
            month = month,
            onPrev = { month = month.minusMonths(1) },
            onNext = { month = month.plusMonths(1) },
        )
        WeekdayHeader()
        MonthGrid(month = month, workoutDays = workoutDays, today = today)
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.stats_calendar_prev_month),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = month.format(MONTH_LABEL_FORMATTER),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.stats_calendar_next_month),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        WEEK.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    workoutDays: Set<Long>,
    today: LocalDate,
) {
    // Monday-based leading blanks: DayOfWeek.MONDAY.value == 1.
    val leadingBlanks = month.atDay(1).dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val cellCount = leadingBlanks + daysInMonth
    val rows = (cellCount + 6) / 7 // ceil to whole weeks

    Column(modifier = Modifier.fillMaxWidth()) {
        for (rowIndex in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (columnIndex in 0 until 7) {
                    val cellIndex = rowIndex * 7 + columnIndex
                    val dayOfMonth = cellIndex - leadingBlanks + 1
                    val cellModifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp)
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = month.atDay(dayOfMonth)
                        DayCell(
                            dayOfMonth = dayOfMonth,
                            isWorkout = date.toEpochDay() in workoutDays,
                            isToday = date == today,
                            modifier = cellModifier,
                        )
                    } else {
                        Box(cellModifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayOfMonth: Int,
    isWorkout: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    var cell = modifier.clip(CircleShape)
    if (isWorkout) {
        cell = cell.background(colorScheme.primary)
    }
    if (isToday) {
        // Outline today; use a contrasting ring when it also sits on a filled day.
        val outline = if (isWorkout) colorScheme.onPrimary else colorScheme.primary
        cell = cell.border(width = 1.5.dp, color = outline, shape = CircleShape)
    }
    Box(modifier = cell, contentAlignment = Alignment.Center) {
        Text(
            text = dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isWorkout) colorScheme.onPrimary else colorScheme.onSurface,
        )
    }
}

/** Column order for the grid: Monday first through Sunday. */
private val WEEK: List<DayOfWeek> = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

private val MONTH_LABEL_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

@Preview(showBackground = true)
@Composable
private fun GymCalendarPreview() {
    val today = LocalDate.now()
    GymSeshTheme {
        GymCalendar(
            workoutDays = setOf(
                today.toEpochDay(),
                today.minusDays(1).toEpochDay(),
                today.minusDays(2).toEpochDay(),
                today.minusDays(6).toEpochDay(),
                today.minusDays(9).toEpochDay(),
            ),
            today = today,
        )
    }
}
