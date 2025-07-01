package com.sonnenstahl.audioman

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonnenstahl.audioman.ui.theme.DarkPlotBackGround
import com.sonnenstahl.audioman.ui.theme.LightPlotBackGround
import com.sonnenstahl.audioman.utils.AudioPlayer
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun normalizeAngle360(angle: Float): Float {
    var normalized = angle % 360f
    if (normalized < 0) {
        normalized += 360f
    }
    return normalized
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimerUi(
    context: Context,
    initialMinutes: Int,
    handleColor: Color,
    inactiveBarColor: Color,
    activeBarColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 5.dp,
) {
    val darkMode = isSystemInDarkTheme()
    val maxMinutes = 90
    var setMinutes by rememberSaveable { mutableStateOf(initialMinutes.coerceIn(0, maxMinutes)) }
    val totalTimeMillisFromSetMinutes = rememberSaveable(setMinutes) { setMinutes * 60 * 1000L }

    var size by remember { mutableStateOf(IntSize.Zero) }
    var currentRemainingMillis by rememberSaveable { mutableStateOf(totalTimeMillisFromSetMinutes) }
    var isTimerRunning by rememberSaveable { mutableStateOf(false) }
    var hasTimerStarted by rememberSaveable { mutableStateOf(false) }

    val startAngle = -215f
    val sweepAngle = 250f

    LaunchedEffect(setMinutes) {
        if (!isTimerRunning) {
            currentRemainingMillis = totalTimeMillisFromSetMinutes
        }
    }

    LaunchedEffect(key1 = currentRemainingMillis, key2 = isTimerRunning) {
        if (currentRemainingMillis > 0 && isTimerRunning) {
            val decrementAmount = 100L
            delay(decrementAmount)
            currentRemainingMillis -= decrementAmount
            if (currentRemainingMillis < 0) {
                currentRemainingMillis = 0L
            }
            if (!hasTimerStarted && AudioPlayer.getSound().id != "-1") {
                AudioPlayer.turnOnTimer(currentRemainingMillis)
                hasTimerStarted = true
            }
        } else if (currentRemainingMillis <= 0 && isTimerRunning) {
            isTimerRunning = false
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .onSizeChanged { size = it },
    ) {
        val diameter = size.width.toFloat()
        val radius = diameter / 2f
        val center = Offset(radius, radius)

        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isTimerRunning = false
                            },
                            onDrag = { change, _ ->
                                if (!isTimerRunning) { // Only allow adjustment if timer is not running
                                    val offset = change.position
                                    val x = offset.x - center.x
                                    val y = offset.y - center.y

                                    // Calculate raw angle in degrees (-180 to 180)
                                    var angle = atan2(y, x) * (180f / PI).toFloat()

                                    // Normalize angle to 0-360
                                    val normalizedTouchAngle = normalizeAngle360(angle)

                                    // Calculate the angle relative to the start of our arc
                                    // The arc goes from -215 (normalized to 145) to 35 (normalized to 35)
                                    // This range wraps around 0/360 degrees.
                                    var relativeToStartAngle: Float
                                    val normalizedArcStart = normalizeAngle360(startAngle) // e.g., 145
                                    val normalizedArcEnd = normalizeAngle360(startAngle + sweepAngle) // e.g., 35

                                    if (normalizedArcEnd > normalizedArcStart) { // Arc does not cross 0/360 (unlikely for this specific arc but good for robustness)
                                        relativeToStartAngle = (normalizedTouchAngle - normalizedArcStart).coerceIn(0f, sweepAngle)
                                    } else { // Arc crosses 0/360 (our case: 145 -> 360 -> 0 -> 35)
                                        if (normalizedTouchAngle >= normalizedArcStart && normalizedTouchAngle <= 360f) {
                                            // User is in the segment from normalizedArcStart to 360
                                            relativeToStartAngle = normalizedTouchAngle - normalizedArcStart
                                        } else if (normalizedTouchAngle >= 0f && normalizedTouchAngle <= normalizedArcEnd) {
                                            // User is in the segment from 0 to normalizedArcEnd
                                            relativeToStartAngle = (360f - normalizedArcStart) + normalizedTouchAngle
                                        } else {
                                            // User is outside the active arc range. Clamp to closest end.
                                            // For simplicity and avoiding complex angle distance, we can clamp based on which side they are closer to.
                                            // This is a heuristic, perfect angle clamping is more involved.
                                            relativeToStartAngle =
                                                if (normalizedTouchAngle > normalizedArcEnd && normalizedTouchAngle < normalizedArcStart) {
                                                    // Assume user is trying to set outside range, snap to closest end of the active arc.
                                                    // If angle is past the end (35 deg) but before start (145 deg), snap to start/end based on proximity.
                                                    val distToStart = (360f - normalizedTouchAngle + normalizedArcStart) % 360f // Distance counter-clockwise to start
                                                    val distToEnd = (normalizedTouchAngle - normalizedArcEnd + 360f) % 360f // Distance clockwise to end

                                                    if (distToStart < distToEnd) 0f else sweepAngle
                                                } else {
                                                    0f // Default to 0 if angle is unhandled (e.g., in the large inactive section)
                                                }
                                        }
                                    }

                                    // Calculate minutes based on relative angle within the sweep
                                    // *** UPDATED: Max minutes is used here ***
                                    val calculatedMinutes = (relativeToStartAngle / sweepAngle * maxMinutes).roundToInt()

                                    setMinutes = calculatedMinutes.coerceIn(0, maxMinutes)
                                    currentRemainingMillis = setMinutes * 60 * 1000L // Update for immediate visual feedback
                                }
                            },
                            onDragEnd = {
                                println("Timer: Drag ended. Set to $setMinutes minutes.")
                            },
                        )
                    },
        ) {
            val currentProgressValue =
                when {
                    isTimerRunning && totalTimeMillisFromSetMinutes > 0 -> currentRemainingMillis.toFloat() / totalTimeMillisFromSetMinutes
                    !isTimerRunning && maxMinutes > 0 -> setMinutes.toFloat() / maxMinutes
                    else -> 0f
                }.coerceIn(0f, 1f)
            drawArc(
                color = inactiveBarColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                size = Size(diameter, diameter),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
            )

            drawArc(
                color = activeBarColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle * currentProgressValue,
                useCenter = false,
                size = Size(diameter, diameter),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
            )

            val handleAngle = startAngle + (sweepAngle * currentProgressValue)
            val beta = handleAngle * (PI / 180f).toFloat()
            val handleX = center.x + cos(beta) * radius
            val handleY = center.y + sin(beta) * radius

            drawPoints(
                listOf(Offset(handleX, handleY)),
                pointMode = PointMode.Points,
                color = handleColor,
                strokeWidth = (strokeWidth * 3f).toPx(),
                cap = StrokeCap.Round,
            )
        }

        val displayText =
            remember(currentRemainingMillis, isTimerRunning, setMinutes) {
                if (isTimerRunning) {
                    val totalSeconds = currentRemainingMillis / 1000L
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    String.format("%02d:%02d", minutes, seconds)
                } else {
                    String.format("%02d:00", setMinutes)
                }
            }

        Text(
            text = displayText,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = if (darkMode) DarkPlotBackGround else LightPlotBackGround,
        )

        Button(
            onClick = {
                if (!AudioPlayer.isPlaying()) {
                    val vibrator =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                        } else {
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        }
                    vibrator.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))

                    Toast.makeText(context, "Cannot set timer if nothing is playing", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (isTimerRunning) {
                    isTimerRunning = false
                } else {
                    if (currentRemainingMillis <= 0L) {
                        if (setMinutes == 0) setMinutes = 1
                        currentRemainingMillis = setMinutes * 60 * 1000L
                    }
                    isTimerRunning = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            colors =
                ButtonDefaults.buttonColors(
                    backgroundColor =
                        when {
                            isTimerRunning -> Color.Red
                            currentRemainingMillis > 0L -> Color.Green
                            else -> Color.Green
                        },
                ),
        ) {
            Text(
                text =
                    when {
                        isTimerRunning -> "Stop"
                        currentRemainingMillis > 0L -> "Start"
                        else -> "Restart"
                    },
            )
        }
    }
}
