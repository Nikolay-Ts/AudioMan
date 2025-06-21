package com.sonnenstahl.audioman

import android.graphics.Bitmap
import java.io.File

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalDrawer
import androidx.compose.runtime.getValue
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.sonnenstahl.audioman.AnimatedPause
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.CURRENT_SOUND_PATH
import com.sonnenstahl.audioman.utils.fallBackSound
import com.sonnenstahl.audioman.utils.loadSound

const val PLAYING_IMAGE_SIZE: Int = 250;
const val PAUSED_IMAGE_SIZE: Int = (PLAYING_IMAGE_SIZE*0.75).toInt()

@OptIn(UnstableApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val isPlaying = remember { mutableStateOf(AudioPlayer.isPlaying()) }
    AudioPlayer.setSound(loadSound(context, CURRENT_SOUND_PATH))
    val currentlyPLaying = AudioPlayer.getSound()
    val imageSize by animateDpAsState(
        targetValue = if (isPlaying.value) PLAYING_IMAGE_SIZE.dp else PAUSED_IMAGE_SIZE.dp,
        label = "imageSizeAnimation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (currentlyPLaying.imagePath.substringAfter(".", "") == "svg") {
            SvgImageFromAssets(
                currentlyPLaying.imagePath,
                modifier = Modifier.size(imageSize)
            )
        } else {
            val imagePath = currentlyPLaying.imagePath
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
                        Image(
                            bitmap = assetBitmap.asImageBitmap(),
                            contentDescription = "Asset Image",
                            modifier = Modifier
                                .size(imageSize)
                                .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(currentlyPLaying.title)
                Text(currentlyPLaying.description)

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