package com.sonnenstahl.audioman

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun FrequencyGraph(
    samples: ByteArray,
    lineColor: MutableState<Color>
) {
    val sampleCount = samples.size / 2
    val points = IntArray(sampleCount) {
        val low = samples[it * 2].toInt() and 0xFF
        val high = samples[it * 2 + 1].toInt()
        (high shl 8) or low
    }

    val maxVal = points.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val configuration = LocalConfiguration.current
    val plotSize = configuration.screenWidthDp * 0.75f

    Canvas(
        modifier = Modifier
            .height(plotSize.dp)
            .width(plotSize.dp)
            .padding(bottom = 8.dp)
    ) {
        val step = size.width / sampleCount
        val horizontalLines = 4
        val verticalLines = 8

        // background
        drawRect(color = Color(0xFFF8F8F8)) // Light gray/white

        // horizontal grid lines
        for (i in 0..horizontalLines) {
            val y = i * size.height / horizontalLines
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5f
            )
        }

        // vertical grid lines
        for (i in 0..verticalLines) {
            val x = i * size.width / verticalLines
            drawLine(
                color = Color.LightGray,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.5f
            )
        }

        // Waveform line
        for (i in 0 until sampleCount - 1) {
            val x1 = i * step
            val x2 = (i + 1) * step

            val y1 = size.height / 2 - (points[i] / maxVal * size.height / 2)
            val y2 = size.height / 2 - (points[i + 1] / maxVal * size.height / 2)

            drawLine(
                color = lineColor.value,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 1.5f
            )
        }
    }
}