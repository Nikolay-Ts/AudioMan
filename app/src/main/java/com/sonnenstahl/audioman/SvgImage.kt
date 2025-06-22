package com.sonnenstahl.audioman

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.SVG
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.delay


/**
 * convert the svg to a bitmap and convert and display it as an Image
 */
@Composable
fun SvgImageFromAssets(
    filepath: String,
    modifier: Modifier = Modifier,
    scale: Float = 10f,
    description: String? = null
) {
    val context = LocalContext.current
    val bitmap by remember(filepath) {
        mutableStateOf(renderSvgToBitmap(context, filepath, scale))
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = description,
        modifier = modifier
    )
}

/**
 * convert th svg to just a regular bitmap
 */
fun renderSvgToBitmap(context: Context, filename: String, scale: Float): Bitmap {
    val inputStream = context.assets.open(filename)
    val svg = SVG.getFromInputStream(inputStream)

    val viewBox = svg.documentViewBox
    val width = (viewBox?.width() ?: 24f).toInt()
    val height = (viewBox?.height() ?: 24f).toInt()

    val bitmap = createBitmap((width * scale).toInt(), (height * scale).toInt())
    val canvas = Canvas(bitmap)
    svg.documentWidth = bitmap.width.toFloat()
    svg.documentHeight = bitmap.height.toFloat()
    svg.renderToCanvas(canvas)

    return bitmap
}

/**
 * Little animated button to give better UI feel and make it a bit more responsive
 */
@Composable
fun AnimatedPause(
    isPlaying: Boolean,
    size: Int = 64,
    onToggle: () -> Unit
) {
    var tapped by remember { mutableStateOf(false) }

    // Slight scale effect on toggle (like a bounce)
    val scale by animateFloatAsState(
        targetValue = if (tapped) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "ScaleAnimation"
    )

    Box(
        modifier = Modifier
            .size(size.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // disables the ripple/rectangle
            ) {
                tapped = true
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isPlaying,
            label = "IconCrossfade"
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                modifier = Modifier.fillMaxSize(),
                tint = Color.White
            )
        }
    }


    LaunchedEffect(tapped) {
        if (tapped) {
            delay(150)
            tapped = false
        }
    }
}