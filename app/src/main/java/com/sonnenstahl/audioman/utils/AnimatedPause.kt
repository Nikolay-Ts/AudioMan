package com.sonnenstahl.audioman.utils

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * Little animated button to give better UI feel and make it a bit more responsive
 */
@Composable
fun AnimatedPause(
    isPlaying: Boolean,
    onToggle: () -> Unit
) {
    var tapped by remember { mutableStateOf(false) }

    // Slight scale effect on toggle (like a bounce)
    val scale by animateFloatAsState(
        targetValue = if (tapped) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "ScaleAnimation"
    )

    Crossfade(
        targetState = isPlaying,
        label = "IconCrossfade"
    ) { playing ->
        Icon(
            imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (playing) "Pause" else "Play",
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clickable {
                    tapped = true
                    onToggle()
                }
        )
    }

    LaunchedEffect(tapped) {
        if (tapped) {
            kotlinx.coroutines.delay(150)
            tapped = false
        }
    }
}