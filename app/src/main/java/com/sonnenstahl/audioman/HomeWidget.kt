package com.sonnenstahl.audioman

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri // Import Uri for parsing paths
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Hello() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sound = AudioPlayer.soundFlow.collectAsState()
    val isPLaying = AudioPlayer.isPlaying.collectAsState()
    val playPauseIcon = if (isPLaying.value) R.drawable.pause else R.drawable.play
    val imageProvider: ImageProvider = remember(sound.value.imagePath) {
        val currentImagePath = sound.value.imagePath
        Log.d("HomeWidget", "Attempting to load image from path: $currentImagePath")

        if (currentImagePath.isEmpty() || currentImagePath == "/default_white.png") {
            Log.d("HomeWidget", "Using default image as path is empty or a known default resource.")
            ImageProvider(R.drawable.default_black)
        } else {
            val uri = try {
                Uri.parse(currentImagePath)
            } catch (e: Exception) {
                Log.e("HomeWidget", "Failed to parse URI from path: $currentImagePath", e)
                null
            }
            if (uri != null && uri.scheme == "content") {
                try {
                    context.contentResolver.openInputStream(uri)?.use { initialStream ->
                        val inSampleSize = calculateInSampleSize(initialStream, 150, 150)
                        context.contentResolver.openInputStream(uri)?.use { finalStream ->
                            val options = BitmapFactory.Options()
                            options.inSampleSize = inSampleSize
                            val bitmap = BitmapFactory.decodeStream(finalStream, null, options)
                            if (bitmap != null) {
                                Log.d("HomeWidget", "Successfully loaded bitmap from URI: $uri (size: ${bitmap.width}x${bitmap.height}, inSampleSize: ${inSampleSize})")
                                val matrix = Matrix()
                                matrix.postRotate(90F)
                                val rotated =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)
                                ImageProvider(rotated)
                            } else {
                                Log.e("HomeWidget", "Failed to decode bitmap from URI: $uri. Using default.")
                                ImageProvider(R.drawable.default_black)
                            }
                        } ?: run {
                            Log.e("HomeWidget", "Failed to open second input stream for URI: $uri. Using default.")
                            ImageProvider(R.drawable.default_black)
                        }
                    } ?: run {
                        Log.e("HomeWidget", "Failed to open initial input stream for URI: $uri. Using default.")
                        ImageProvider(R.drawable.default_black)
                    }
                } catch (e: SecurityException) {
                    Log.e("HomeWidget", "Security exception accessing URI: $uri. Check permissions! Using default.", e)
                    Toast.makeText(context, "Permission denied to access image.", Toast.LENGTH_LONG).show()
                    ImageProvider(R.drawable.default_black)
                } catch (e: Exception) {
                    Log.e("HomeWidget", "Error loading bitmap from URI: $uri. Using default.", e)
                    ImageProvider(R.drawable.default_black)
                }
            } else {
                val imageFile = File(currentImagePath)
                if (imageFile.exists() && imageFile.isFile) {
                    try {
                        val inSampleSize = calculateInSampleSize(imageFile.inputStream(), 150, 150)
                        val options = BitmapFactory.Options()
                        options.inSampleSize = inSampleSize
                        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)

                        if (bitmap != null) {
                            Log.d("HomeWidget", "Successfully loaded bitmap from file: ${imageFile.absolutePath} (size: ${bitmap.width}x${bitmap.height}, inSampleSize: ${inSampleSize})")
                            val matrix = Matrix()
                            matrix.postRotate(90F)
                            val rotated =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)
                            ImageProvider(rotated)
                        } else {
                            Log.e("HomeWidget", "Failed to decode bitmap from file: ${imageFile.absolutePath}. Using default.")
                            ImageProvider(R.drawable.default_black)
                        }
                    } catch (e: Exception) {
                        Log.e("HomeWidget", "Error loading image from file: ${imageFile.absolutePath}. Using default.", e)
                        ImageProvider(R.drawable.default_black)
                    }
                } else {
                    Log.w("HomeWidget", "Image file not found or not a file: ${imageFile.absolutePath}. Using default.")
                    ImageProvider(R.drawable.default_black)
                }
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
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {

                Image(
                    provider = imageProvider,
                    contentDescription = "Play/Pause",
                    modifier =
                        GlanceModifier
                            .padding(start = 8.dp)
                            .size(50.dp)
                )

            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(sound.value.title)
                Text(sound.value.description)
            }

            Image(
                provider = ImageProvider(playPauseIcon),
                contentDescription = "Play/Pause",
                modifier =
                    GlanceModifier
                        .padding(start = 8.dp)
                        .size(50.dp)
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