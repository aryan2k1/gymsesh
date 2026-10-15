package com.gymsesh.app.ui.screens.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gymsesh.app.R
import com.gymsesh.app.ui.theme.GymSeshTheme
import kotlinx.coroutines.delay

/** Default rest length and the preset chips offered under the timer. */
const val REST_TIMER_DEFAULT_SECONDS: Int = 90
private val REST_PRESETS = listOf(60, 90, 120, 180)
private const val REST_STEP_SECONDS = 15
private const val REST_MAX_SECONDS = 60 * 99

/**
 * Small, self-contained countdown state for the rest timer. Values are Compose
 * state so the composable recomposes as time ticks; the actual ticking loop is
 * owned by [RestTimer] (a coroutine + [delay]) so it is cancelled — i.e. paused
 * — automatically when the composable leaves composition.
 */
@Stable
class RestTimerState(default: Int = REST_TIMER_DEFAULT_SECONDS) {
    var presetSeconds by mutableIntStateOf(default)
        private set
    var remainingSeconds by mutableIntStateOf(default)
        private set
    var isRunning by mutableStateOf(false)
        private set

    val isFinished: Boolean get() = remainingSeconds <= 0

    fun start() {
        if (remainingSeconds <= 0) remainingSeconds = presetSeconds
        isRunning = true
    }

    fun pause() {
        isRunning = false
    }

    fun toggle() {
        if (isRunning) pause() else start()
    }

    fun reset() {
        isRunning = false
        remainingSeconds = presetSeconds
    }

    fun setPreset(seconds: Int) {
        presetSeconds = seconds.coerceIn(REST_STEP_SECONDS, REST_MAX_SECONDS)
        remainingSeconds = presetSeconds
        isRunning = false
    }

    fun adjust(deltaSeconds: Int) {
        presetSeconds = (presetSeconds + deltaSeconds).coerceIn(REST_STEP_SECONDS, REST_MAX_SECONDS)
        remainingSeconds = (remainingSeconds + deltaSeconds).coerceIn(0, REST_MAX_SECONDS)
    }

    /** Called by the ticking loop each second. */
    internal fun tick() {
        if (remainingSeconds > 0) remainingSeconds--
    }

    internal fun stopAtZero() {
        isRunning = false
    }
}

@Composable
fun rememberRestTimerState(default: Int = REST_TIMER_DEFAULT_SECONDS): RestTimerState =
    remember { RestTimerState(default) }

private fun formatTime(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val minutes = safe / 60
    val seconds = safe % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun RestTimer(
    modifier: Modifier = Modifier,
    state: RestTimerState = rememberRestTimerState(),
) {
    val haptics = LocalHapticFeedback.current

    // Ticking loop. Keyed on isRunning so it restarts on resume; cancelled on
    // dispose (navigating away) which effectively pauses without extra wiring.
    LaunchedEffect(state.isRunning) {
        if (!state.isRunning) return@LaunchedEffect
        while (state.isRunning && state.remainingSeconds > 0) {
            delay(1_000L)
            state.tick()
        }
        if (state.remainingSeconds <= 0 && state.isRunning) {
            state.stopAtZero()
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val timeColor = when {
        state.isFinished -> MaterialTheme.colorScheme.error
        state.isRunning -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.log_rest_timer),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatTime(state.remainingSeconds),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    color = timeColor,
                )
            }

            // Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                REST_PRESETS.forEach { preset ->
                    FilterChip(
                        selected = state.presetSeconds == preset,
                        onClick = { state.setPreset(preset) },
                        label = {
                            Text(stringResource(R.string.rest_preset_seconds, preset))
                        },
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { state.adjust(-REST_STEP_SECONDS) }) {
                    Text(stringResource(R.string.rest_minus_step))
                }
                FilledTonalButton(
                    onClick = { state.toggle() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        stringResource(
                            if (state.isRunning) R.string.rest_pause else R.string.rest_start,
                        ),
                    )
                }
                OutlinedButton(onClick = { state.adjust(REST_STEP_SECONDS) }) {
                    Text(stringResource(R.string.rest_plus_step))
                }
                OutlinedButton(onClick = { state.reset() }) {
                    Text(stringResource(R.string.rest_reset))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Rest timer")
@Composable
private fun RestTimerPreview() {
    GymSeshTheme {
        RestTimer(modifier = Modifier.padding(16.dp))
    }
}
