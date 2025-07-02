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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import com.sonnenstahl.audioman.ui.theme.DarkPlotBackGround
import com.sonnenstahl.audioman.ui.theme.LightPlotBackGround
import com.sonnenstahl.audioman.utils.AudioPlayer
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun normalizeAngle(angle: Float, startRange: Float = -180f, endRange: Float = 180f): Float {
    var result = angle
    val range = endRange - startRange
    while (result < startRange) result += range
    while (result >= endRange) result -= range
    return result
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
    val audioPlayerCurrentRemainingMillis by AudioPlayer.sleepTimeMilli.collectAsStateWithLifecycle()
    val audioPlayerIsActive by AudioPlayer.isActive.collectAsStateWithLifecycle()
    var setMinutes by rememberSaveable { mutableStateOf(initialMinutes.coerceIn(0, maxMinutes)) }
    var initialTotalSetMinutes by rememberSaveable { mutableStateOf(initialMinutes.coerceIn(0, maxMinutes)) }
    var isDragging by rememberSaveable { mutableStateOf(false) }
    var touchOffsetFromHandle by remember { mutableStateOf(0f) }


    LaunchedEffect(Unit) {
        val currentAudioPlayerMinutes = (AudioPlayer.sleepTimeMilli.value / 60_000L).toInt()
        val currentAudioPlayerIsActive = AudioPlayer.isActive.value

        if (currentAudioPlayerIsActive || currentAudioPlayerMinutes > 0L) {
            setMinutes = currentAudioPlayerMinutes
            initialTotalSetMinutes = currentAudioPlayerMinutes.coerceAtLeast(1)
        } else {
            setMinutes = initialMinutes.coerceIn(0, maxMinutes)
            initialTotalSetMinutes = initialMinutes.coerceIn(0, maxMinutes)
        }
        Log.d("TimerUi", "Initial LaunchedEffect Sync: setMinutes=$setMinutes, initialTotalSetMinutes=$initialTotalSetMinutes, AudioPlayer active=$currentAudioPlayerIsActive, remaining=${AudioPlayer.sleepTimeMilli.value/1000}s")
    }


    LaunchedEffect(audioPlayerCurrentRemainingMillis, audioPlayerIsActive, isDragging) {
        Log.d("TimerUi", "Continuous LaunchedEffect: audioPlayerCurrentRemainingMillis=${audioPlayerCurrentRemainingMillis/1000}s, audioPlayerIsActive=$audioPlayerIsActive, isDragging=$isDragging")

        if (audioPlayerIsActive && !isDragging) {
            val minutesFromAudioPlayer = (audioPlayerCurrentRemainingMillis / 60_000L).toInt()
            if (kotlin.math.abs(setMinutes - minutesFromAudioPlayer) > 0) {
                setMinutes = minutesFromAudioPlayer
                Log.d("TimerUi", "Continuous Sync: Updated setMinutes to $setMinutes from AudioPlayer (active).")
            }
        } else if (!audioPlayerIsActive && !isDragging) {
            if (audioPlayerCurrentRemainingMillis <= 0L) {
                if (setMinutes != 0) {
                    setMinutes = 0
                    Log.d("TimerUi", "Continuous Sync: Timer finished, setMinutes reset to 0.")
                }
                if (initialTotalSetMinutes != 0) {
                    initialTotalSetMinutes = 0
                    Log.d("TimerUi", "Continuous Sync: Timer finished, initialTotalSetMinutes reset to 0.")
                }
            } else {
                val minutesFromAudioPlayer = (audioPlayerCurrentRemainingMillis / 60_000L).toInt()
                if (kotlin.math.abs(setMinutes - minutesFromAudioPlayer) > 0) {
                    setMinutes = minutesFromAudioPlayer
                    Log.d("TimerUi", "Continuous Sync: Updated setMinutes to $setMinutes from AudioPlayer (paused).")
                }
            }
        }
    }


    var size by remember { mutableStateOf(IntSize.Zero) }
    val coroutineScope = rememberCoroutineScope()

    val startAngle = -215f
    val sweepAngle = 250f

    val dialProgressValue = remember(audioPlayerCurrentRemainingMillis, audioPlayerIsActive, setMinutes, maxMinutes) {
        if (audioPlayerIsActive) {
            audioPlayerCurrentRemainingMillis.toFloat() / (maxMinutes * 60 * 1000L).coerceAtLeast(1L)
        } else {
            setMinutes.toFloat() / maxMinutes.toFloat()
        }
    }.coerceIn(0f, 1f)


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
                            onDragStart = { offset ->
                                isDragging = true
                                coroutineScope.launch {
                                    AudioPlayer.pauseTimer()
                                }

                                val handleAngle = startAngle + (sweepAngle * (setMinutes.toFloat() / maxMinutes))
                                val touchAngle = atan2(offset.y - center.y, offset.x - center.y) * (180f / PI).toFloat()

                                touchOffsetFromHandle = normalizeAngle(touchAngle - handleAngle, -180f, 180f)
                                Log.d("TimerDrag", "Drag Start: setMinutes=$setMinutes, handleAngle=$handleAngle, touchAngle=$touchAngle, touchOffsetFromHandle=$touchOffsetFromHandle")
                            },
                            onDrag = { change, _ ->
                                val offset = change.position
                                val x = offset.x - center.x
                                val y = offset.y - center.y

                                val currentTouchAngle = atan2(y, x) * (180f / PI).toFloat()

                                val targetHandleAngle = currentTouchAngle - touchOffsetFromHandle

                                val angleRelativeToStartOfArc = normalizeAngle(targetHandleAngle - startAngle, -180f, 180f)

                                val clampedRelativeAngle = angleRelativeToStartOfArc.coerceIn(0f, sweepAngle)

                                val calculatedMinutes = (clampedRelativeAngle / sweepAngle * maxMinutes).roundToInt()

                                if (setMinutes != calculatedMinutes) {
                                    setMinutes = calculatedMinutes.coerceIn(0, maxMinutes)
                                    coroutineScope.launch {
                                        AudioPlayer.turnOnTimer(setMinutes * 60 * 1000L)
                                        AudioPlayer.pauseTimer()
                                    }
                                }
                                Log.d("TimerDrag", "Dragging: setMinutes=$setMinutes, currentTouchAngle=$currentTouchAngle, targetHandleAngle=$targetHandleAngle, clampedRelativeAngle=$clampedRelativeAngle")
                            },
                            onDragEnd = {
                                isDragging = false
                                println("Timer: Drag ended. Set to $setMinutes minutes.")
                                initialTotalSetMinutes = setMinutes
                                Log.d("TimerUi", "onDragEnd: initialTotalSetMinutes updated to $initialTotalSetMinutes")
                            },
                            onDragCancel = {
                                isDragging = false
                                println("Timer: Drag cancelled. Set to $setMinutes minutes.")
                                initialTotalSetMinutes = setMinutes
                                Log.d("TimerUi", "onDragCancel: initialTotalSetMinutes updated to $initialTotalSetMinutes")
                            }
                        )
                    },
        ) {
            // Use the pre-calculated dialProgressValue here
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
                sweepAngle = sweepAngle * dialProgressValue,
                useCenter = false,
                size = Size(diameter, diameter),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
            )

            val handleAngle = startAngle + (sweepAngle * dialProgressValue)
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
            remember(audioPlayerCurrentRemainingMillis, audioPlayerIsActive, setMinutes, isDragging) {
                if (isDragging || !audioPlayerIsActive) {
                    "%02d:00".format(setMinutes)
                } else {
                    val totalSeconds = audioPlayerCurrentRemainingMillis / 1000L
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    "%02d:%02d".format(minutes, seconds)
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
                        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
                            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                        } else {
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        }
                    vibrator.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))

                    Toast.makeText(context, "Cannot set timer if nothing is playing", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                coroutineScope.launch {
                    if (audioPlayerIsActive) {
                        AudioPlayer.pauseTimer()
                    } else {
                        val timeToSet = (setMinutes.coerceAtLeast(1)) * 60 * 1000L

                        initialTotalSetMinutes = setMinutes.coerceAtLeast(1)
                        Log.d("TimerUi", "Button Click: Setting initialTotalSetMinutes to $initialTotalSetMinutes")

                        AudioPlayer.turnOnTimer(timeToSet)

                        AudioPlayer.countDown()
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            colors =
                ButtonDefaults.buttonColors(
                    backgroundColor =
                        when {
                            audioPlayerIsActive -> Color.Red
                            audioPlayerCurrentRemainingMillis > 0L -> Color.Green
                            else -> Color.Green
                        },
                ),
        ) {
            Text(
                text =
                    when {
                        audioPlayerIsActive -> "Stop"
                        else -> "Start"
                    },
            )
        }
    }
}