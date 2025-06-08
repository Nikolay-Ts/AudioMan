package com.sonnenstahl.audioman

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.caverock.androidsvg.SVG
import androidx.core.graphics.createBitmap


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