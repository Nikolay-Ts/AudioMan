package com.sonnenstahl.audioman

import android.graphics.BitmapFactory
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

        // if the image is an svg load it as one, otherwise as a regular Image
        if (currentlyPLaying.imagePath.substringAfter(".", "") == "svg") {
            SvgImageFromAssets(
                currentlyPLaying.imagePath,
                modifier = Modifier.size(120.dp)
            )
        } else {
            val bitmap = BitmapFactory.decodeStream(context.assets.open(currentlyPLaying.imagePath))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Asset Image",
                modifier = Modifier.size(120.dp)
            )
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