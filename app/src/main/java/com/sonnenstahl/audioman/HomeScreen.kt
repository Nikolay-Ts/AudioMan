package com.sonnenstahl.audioman

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.ui.theme.LightTeal
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.CURRENT_SOUND_PATH
import com.sonnenstahl.audioman.utils.DEFAULT_IMAGE_URI
import com.sonnenstahl.audioman.utils.DEFAULT_LIGHT_IMAGE
import com.sonnenstahl.audioman.utils.loadSound
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.io.File

const val PLAYING_IMAGE_SIZE: Int = 250
const val PAUSED_IMAGE_SIZE: Int = (PLAYING_IMAGE_SIZE * 0.75).toInt()

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val displayTimer = remember { mutableStateOf(false) }
    val coroutinScope = rememberCoroutineScope()
    val darkMode = isSystemInDarkTheme()

    val isPlaying by AudioPlayer.isPlaying.collectAsStateWithLifecycle()
    val currentlyPlaying = remember { mutableStateOf(AudioPlayer.getSound()) }
    val volume = rememberSaveable { mutableStateOf(AudioPlayer.getVolume()) }
    var imagePath by remember { mutableStateOf(DEFAULT_IMAGE_URI) }
    val currentSound = remember { mutableStateOf(loadSound(context, CURRENT_SOUND_PATH)) }

    val imageSize by animateDpAsState(
        targetValue = if (isPlaying) PLAYING_IMAGE_SIZE.dp else PAUSED_IMAGE_SIZE.dp,
        label = "imageSizeAnimation",
    )

    imagePath =
        if (currentlyPlaying.value.imagePath == DEFAULT_IMAGE_URI || currentlyPlaying.value.imagePath == DEFAULT_LIGHT_IMAGE) {
            if (darkMode) DEFAULT_LIGHT_IMAGE else DEFAULT_IMAGE_URI
        } else {
            currentlyPlaying.value.imagePath
        }

    val imageFile = File(imagePath)
    val rotatedBitmap by produceState<Bitmap?>(initialValue = null, key1 = imagePath) {
        value =
            runCatching {
                val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                val exif = ExifInterface(imageFile.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 ->
                        Bitmap
                            .createBitmap(
                                originalBitmap,
                                0,
                                0,
                                originalBitmap.width,
                                originalBitmap.height,
                                Matrix().apply {
                                    postRotate(90f)
                                },
                                true,
                            )
                    ExifInterface.ORIENTATION_ROTATE_180 ->
                        Bitmap
                            .createBitmap(
                                originalBitmap,
                                0,
                                0,
                                originalBitmap.width,
                                originalBitmap.height,
                                Matrix().apply {
                                    postRotate(180f)
                                },
                                true,
                            )
                    ExifInterface.ORIENTATION_ROTATE_270 ->
                        Bitmap
                            .createBitmap(
                                originalBitmap,
                                0,
                                0,
                                originalBitmap.width,
                                originalBitmap.height,
                                Matrix().apply {
                                    postRotate(270f)
                                },
                                true,
                            )
                    else -> originalBitmap
                }
            }.getOrNull()
    }

    val assetBitmap =
        if (!imageFile.exists()) {
            runCatching {
                context.assets.open(imagePath).use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        } else {
            null
        }

    // initialise the player
    LaunchedEffect(Unit) {
        AudioPlayer.initialize(context)
        currentSound.value = loadSound(context, CURRENT_SOUND_PATH)
    }

    if (displayTimer.value) {
        SleepTimer(context, { displayTimer.value = false })
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
    ) {
        Text(
            text = "Currently Playing",
            style = MaterialTheme.typography.titleLarge,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
        )

        IconButton(
            onClick = {
                displayTimer.value = true
            },
            modifier =
                Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp),
        ) {
            val imagePath =
                when (darkMode) {
                    true -> "file:///android_asset/timer_light.png"
                    false -> "file:///android_asset/timer.png"
                }

            Image(
                painter = rememberAsyncImagePainter(model = imagePath),
                contentDescription = "Set Sleep Timer",
                modifier = Modifier.size(32.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = rotatedBitmap ?: assetBitmap
            val isDefault =
                when (imagePath) {
                    "android_asset/default_white.png",
                    "android_asset/default.png",
                    DEFAULT_LIGHT_IMAGE,
                    DEFAULT_IMAGE_URI,
                    -> true
                    else -> false
                }

            if (isDefault) {
                val path =
                    when (darkMode) {
                        true -> DEFAULT_LIGHT_IMAGE
                        false -> DEFAULT_IMAGE_URI
                    }
                Image(
                    rememberAsyncImagePainter(model = "file:///android_asset/$path"),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(16.dp)),
                )
            } else if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Album Art",
                    modifier =
                        Modifier
                            .size(imageSize)
                            .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp)),
                )
            }
        }


        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(currentlyPlaying.value.title)
            Text(currentlyPlaying.value.description)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val low =
                    when (isSystemInDarkTheme()) {
                        true -> "low-light-volume.png"
                        false -> "low-volume.png"
                    }

                val high =
                    when (isSystemInDarkTheme()) {
                        true -> "high-light-volume.png"
                        false -> "high-volume.png"
                    }

                Image(
                    painter = rememberAsyncImagePainter(model = "file:///android_asset/$low"),
                    contentDescription = "Low Volume",
                    modifier = Modifier.size(24.dp),
                )

                Slider(
                    value = volume.value ?: 0.5f,
                    onValueChange = { volume.value = it },
                    onValueChangeFinished = {
                        AudioPlayer.setVolume(volume.value ?: 0.5f)
                        if (volume.value == 1.0f || volume.value == 0.0f) {
                            val vibrator =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                                } else {
                                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                }
                            vibrator.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    },
                    valueRange = 0f..1f,
                    modifier =
                        Modifier
                            .weight(3f)
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp),
                    colors =
                        SliderDefaults.colors(
                            thumbColor = Teal,
                            activeTrackColor = LightTeal,
                            inactiveTrackColor = Color.White,
                        ),
                )

                Image(
                    painter = rememberAsyncImagePainter(model = "file:///android_asset/$high"),
                    contentDescription = "High Volume",
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedPause(isPlaying) {
                if (currentlyPlaying.value.id == "-1") { // fallback id
                    val vibrator =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                        } else {
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        }
                    vibrator.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))

                    Toast.makeText(context, "Choose a sound first!", Toast.LENGTH_SHORT).show()
                    return@AnimatedPause
                }

                coroutinScope.launch {
                    when (isPlaying) {
                        true -> AudioPlayer.pause()
                        false -> AudioPlayer.play()
                    }
                }
            }
        }
    }
}
