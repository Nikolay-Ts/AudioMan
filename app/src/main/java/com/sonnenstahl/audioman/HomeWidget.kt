package com.sonnenstahl.audioman

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.sonnenstahl.audioman.ui.theme.DarkPlotBackGround
import com.sonnenstahl.audioman.utils.AudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import androidx.exifinterface.media.ExifInterface
import androidx.glance.appwidget.cornerRadius

import androidx.glance.layout.ContentScale
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.sonnenstahl.audioman.utils.DEFAULT_IMAGE_URI
import com.sonnenstahl.audioman.utils.DEFAULT_LIGHT_IMAGE
import androidx.core.net.toUri


/**
 * @brief widget to be displaye din the home screen. it is 4x1
 */
class HomeWidget : GlanceAppWidget() {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            Hello()
        }
    }
}


/**
 * @brief the content of the image. It displays the current track cover,
 * Along side the title description and the option to pause or play the current audio.
 * if nothing is playing, the widget does not allow you to play or pause and gives the user
 * visual feedback and a vibration
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Hello() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sound = AudioPlayer.soundFlow.collectAsState()
    val isPLaying = AudioPlayer.isPlaying.collectAsState()
    val playPauseIcon = if (isPLaying.value) R.drawable.pause else R.drawable.play
    val imageProvider: ImageProvider by produceState(
        initialValue = ImageProvider(R.drawable.default_black),
        key1 = sound.value.imagePath
    ) {
        value = withContext(Dispatchers.IO) {
            val currentImagePath = sound.value.imagePath
            Log.d("HomeWidget", "ProduceState triggered. Current sound.value.imagePath: '$currentImagePath'")

            // Handle default/empty path immediately
            if (currentImagePath.isEmpty() || currentImagePath == DEFAULT_IMAGE_URI || currentImagePath == DEFAULT_LIGHT_IMAGE) {
                Log.d("HomeWidget", "Path is empty or a known default. Using default ImageProvider.")
                return@withContext ImageProvider(R.drawable.default_black)
            }

            val uri = try {
                currentImagePath.toUri()
            } catch (e: Exception) {
                Log.e("HomeWidget", "Failed to parse URI from path: '$currentImagePath'", e)
                return@withContext ImageProvider(R.drawable.default_black)
            }

            var loadedBitmap: Bitmap? = null
            var orientation = ExifInterface.ORIENTATION_NORMAL // Default to normal orientation

            if (uri.scheme == "content") {
                try {
                    context.contentResolver.openInputStream(uri)?.use { exifStream ->
                        try {
                            val exifInterface = ExifInterface(exifStream)
                            orientation = exifInterface.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                            Log.d("HomeWidget", "Content URI EXIF Orientation: $orientation")
                        } catch (e: Exception) {
                            Log.e("HomeWidget", "Error getting EXIF from content URI: $uri", e)
                        }
                    } ?: run { Log.w("HomeWidget", "Could not open EXIF stream for content URI: $uri") }

                    context.contentResolver.openInputStream(uri)?.use { initialStream ->
                        val inSampleSize = calculateInSampleSize(initialStream, 150, 150)
                        context.contentResolver.openInputStream(uri)?.use { finalStream ->
                            val options = BitmapFactory.Options()
                            options.inSampleSize = inSampleSize
                            loadedBitmap = BitmapFactory.decodeStream(finalStream, null, options)
                            Log.d("HomeWidget", "Decoded bitmap from content URI. Is Null: ${loadedBitmap == null}, InSampleSize: $inSampleSize")
                        } ?: run { Log.e("HomeWidget", "Failed to open second input stream for content URI: $uri") }
                    } ?: run { Log.e("HomeWidget", "Failed to open initial input stream for content URI: $uri") }

                } catch (e: SecurityException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Permission denied to access image.", Toast.LENGTH_LONG).show()
                    }
                    return@withContext ImageProvider(R.drawable.default_black) // Return default on security error
                } catch (e: Exception) {
                    Log.e("HomeWidget", "Generic error loading bitmap from content URI: $uri", e)
                    return@withContext ImageProvider(R.drawable.default_black)
                }
            } else { // Assume it's a file path
                val imageFile = File(currentImagePath)
                if (!imageFile.exists() || !imageFile.isFile) {
                    Log.w("HomeWidget", "Image file not found or not a file: '${imageFile.absolutePath}'. Using default.")
                    return@withContext ImageProvider(R.drawable.default_black)
                }

                try {
                    // get EXIF orientation for file path
                    try {
                        val exifInterface = ExifInterface(imageFile.absolutePath)
                        orientation = exifInterface.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        Log.d("HomeWidget", "File path EXIF Orientation: $orientation")
                    } catch (e: Exception) {
                        Log.e("HomeWidget", "Error getting EXIF from file: '${imageFile.absolutePath}'", e)
                    }

                    // decode bitmap from file path
                    val inSampleSize = calculateInSampleSize(imageFile.inputStream(), 150, 150)
                    val options = BitmapFactory.Options()
                    options.inSampleSize = inSampleSize
                    loadedBitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)

                } catch (e: Exception) {
                    Log.e("HomeWidget", "Error loading image from file: '${imageFile.absolutePath}'", e)
                    return@withContext ImageProvider(R.drawable.default_black)
                }
            }

            // 3. Apply EXIF rotation (or no rotation if orientation is NORMAL/UNDEFINED)
            if (loadedBitmap != null) {
                val finalBitmap = applyExifRotation(loadedBitmap, orientation)
                if (finalBitmap != null) {
                    ImageProvider(finalBitmap)
                } else {
                    ImageProvider(R.drawable.default_black)
                }
            } else {
                ImageProvider(R.drawable.default_black)
            }
        }
    }

    Box(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(DarkPlotBackGround)
                    .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {

            Image(
                provider = imageProvider,
                contentDescription = "Sound-track",
                modifier =
                    GlanceModifier
                        .padding(start = 4.dp)
                        .padding(end = 5.dp)
                        .size(65.dp)
                        .background(Color.Transparent)
                        .cornerRadius(15.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier =
                    GlanceModifier
                        .padding(5.dp)
                        .defaultWeight(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var title = if (sound.value.title.length > 30) {
                    sound.value.title.take(27)+ "..."
                } else {
                    sound.value.title
                }

                val description = if (sound.value.description.length > 35) {
                    sound.value.description.take(33)+ "..."
                } else {
                    sound.value.description
                }

                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight=FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                )
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 15.sp,
                        textAlign = TextAlign.Start
                    )
                )
            }

            Image(
                provider = ImageProvider(playPauseIcon),
                contentDescription = "Play/Pause",
                modifier =
                    GlanceModifier
                        .padding(start = 8.dp)
                        .padding(horizontal = 5.dp)
                        .size(60.dp)
                        .clickable {
                            if (sound.value.id == "-1") {
                                val vibrator =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                                } else {
                                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                }
                                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                                Toast.makeText(context, "Choose a track first", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            coroutineScope.launch {
                                when (isPLaying.value) {
                                    true -> AudioPlayer.pause()
                                    false -> AudioPlayer.play()
                                }
                            }
                        }
            )
        }
    }
}

fun calculateInSampleSize(
    inputStream: InputStream,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true

    BitmapFactory.decodeStream(inputStream, null, options)

    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * applies EXIF orientation rotation to a bitmap.
 *
 * @param bitmap The bitmap to rotate.
 * @param orientation The EXIF orientation tag value (e.g., ExifInterface.ORIENTATION_ROTATE_90).
 * @return The rotated bitmap, or the original bitmap if no rotation is needed or if an error occurs.
 */
fun applyExifRotation(bitmap: Bitmap?, orientation: Int): Bitmap? {
    if (bitmap == null) {
        Log.w("ImageRotation", "Bitmap is null, cannot apply rotation.")
        return null
    }

    val matrix = Matrix()
    var rotated = false

    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> { matrix.postRotate(90f); rotated = true }
        ExifInterface.ORIENTATION_ROTATE_180 -> { matrix.postRotate(180f); rotated = true }
        ExifInterface.ORIENTATION_ROTATE_270 -> { matrix.postRotate(270f); rotated = true }
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> { matrix.preScale(-1.0f, 1.0f); rotated = true }
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> { matrix.preScale(1.0f, -1.0f); rotated = true }
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.preScale(-1.0f, 1.0f)
            rotated = true
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f)
            matrix.preScale(-1.0f, 1.0f)
            rotated = true
        }
        else -> {
            return bitmap
        }
    }

    if (!rotated) {
        return bitmap
    }

    return try {
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        rotatedBitmap
    } catch (e: Exception) {
        bitmap
    }
}