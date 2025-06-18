package com.sonnenstahl.audioman

import android.graphics.Bitmap
import java.io.File

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.sonnenstahl.audioman.AnimatedPause
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.fallBackSound

@OptIn(UnstableApi::class)
@Composable

fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val isPlaying = remember { mutableStateOf(AudioPlayer.isPlaying()) }
    val currentlyPLaying = AudioPlayer.getSound()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentlyPLaying.imagePath.substringAfter(".", "") == "svg") {
            SvgImageFromAssets(
                currentlyPLaying.imagePath,
                modifier = Modifier.size(120.dp)
            )
        } else {
            val imagePath = currentlyPLaying.imagePath
            val imageFile = File(imagePath)

            if (imageFile.exists()) {
                val bitmap = runCatching {
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

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "User Image",
                        modifier = Modifier.size(120.dp)
                    )
                }
            } else {
                val assetBitmap = runCatching {
                    context.assets.open(imagePath).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }.getOrNull()

                if (assetBitmap != null) {
                    Image(
                        bitmap = assetBitmap.asImageBitmap(),
                        contentDescription = "Asset Image",
                        modifier = Modifier.size(120.dp)
                    )
                } else {
                    Log.e("HomeScreen", "Failed to decode asset image: $imagePath")
                }
            }
        }

        Column {
            Text(currentlyPLaying.title)
            Text(currentlyPLaying.description)
        }

        AnimatedPause(isPlaying.value) {

            when (isPlaying.value) {
                true ->  {
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