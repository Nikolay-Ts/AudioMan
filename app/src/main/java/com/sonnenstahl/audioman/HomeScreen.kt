package com.sonnenstahl.audioman

import android.graphics.Bitmap
import java.io.File

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.ui.theme.LightTeal
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.CURRENT_SOUND_PATH
import com.sonnenstahl.audioman.utils.DEFAULT_IMAGE_URI
import com.sonnenstahl.audioman.utils.DEFAULT_LIGHT_IMAGE
import com.sonnenstahl.audioman.utils.loadSound

const val PLAYING_IMAGE_SIZE: Int = 250;
const val PAUSED_IMAGE_SIZE:  Int = (PLAYING_IMAGE_SIZE*0.75).toInt()

@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val isPlaying = remember { mutableStateOf(AudioPlayer.isPlaying()) }
    val currentlyPLaying = remember { mutableStateOf(AudioPlayer.getSound()) }
    val volume = remember { mutableStateOf(AudioPlayer.getVolume()) }

    LaunchedEffect(Unit) {
        AudioPlayer.initialize(context)
        AudioPlayer.setSound(loadSound(context, CURRENT_SOUND_PATH))
        val loadedSound = AudioPlayer.getSound()
        currentlyPLaying.value = loadedSound
    }

    val imageSize by animateDpAsState(
        targetValue = if (isPlaying.value) PLAYING_IMAGE_SIZE.dp else PAUSED_IMAGE_SIZE.dp,
        label = "imageSizeAnimation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "Currently Playing",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        val imagePath = if (currentlyPLaying.value.imagePath == DEFAULT_IMAGE_URI) {
            if (isSystemInDarkTheme()) {
                DEFAULT_LIGHT_IMAGE
            } else {
                DEFAULT_IMAGE_URI
            }
        } else {
            currentlyPLaying.value.imagePath
        }
        val imageFile = File(imagePath)

        if (imageFile.exists()) {
            val rotatedBitmap by produceState<Bitmap?>(initialValue = null, key1 = imagePath) {
                value = runCatching {
                    val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    val exif = ExifInterface(imageFile.absolutePath)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, Matrix().apply { postRotate(90f) }, true)
                        ExifInterface.ORIENTATION_ROTATE_180 -> Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, Matrix().apply { postRotate(180f) }, true)
                        ExifInterface.ORIENTATION_ROTATE_270 -> Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, Matrix().apply { postRotate(270f) }, true)
                        else -> originalBitmap
                    }
                }.getOrNull()
            }

            if (rotatedBitmap != null) {
                Box(
                    modifier = Modifier
                        .height(PLAYING_IMAGE_SIZE.dp)
                ) {
                    Image(
                        bitmap = rotatedBitmap!!.asImageBitmap(),
                        contentDescription = "User Image",
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                    )
                }
            }
        } else {
            val assetBitmap = runCatching {
                context.assets.open(imagePath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }.getOrNull()

            if (assetBitmap != null) {
                Box(
                    modifier = Modifier
                        .height(PLAYING_IMAGE_SIZE.dp)
                ) {
                    val isDefault = imagePath == DEFAULT_IMAGE_URI || imagePath == DEFAULT_LIGHT_IMAGE
                    if (isDefault) {
                        Image(
                            bitmap = assetBitmap.asImageBitmap(),
                            contentDescription = "Asset Image",
                            modifier = Modifier
                                .size(imageSize)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Image(
                            bitmap = assetBitmap.asImageBitmap(),
                            contentDescription = "Asset Image",
                            modifier = Modifier
                                .size(imageSize)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp)
                .padding(top = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(currentlyPLaying.value.title)
                    Text(currentlyPLaying.value.description)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)) {
                    Slider(
                        value = volume.value ?: 0.5f ,
                        onValueChange = { volume.value = it },
                        onValueChangeFinished = { AudioPlayer.setVolume(volume.value ?: 0.5f) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Teal,
                            activeTrackColor = LightTeal,
                            inactiveTrackColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedPause(isPlaying.value) {
                    when (isPlaying.value) {
                        true -> {
                            AudioPlayer.pause()
                            isPlaying.value = false
                        }
                        false -> {
                            AudioPlayer.play()
                            isPlaying.value = true
                        }
                    }
                }

            }
        }
    }
}