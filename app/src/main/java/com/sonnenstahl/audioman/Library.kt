package com.sonnenstahl.audioman

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.SOUNDS_FILE_PATH
import com.sonnenstahl.audioman.utils.Sounds
import com.sonnenstahl.audioman.utils.defaultSounds
import com.sonnenstahl.audioman.utils.deleteSoundsFile
import com.sonnenstahl.audioman.utils.loadSounds
import java.io.File


/**
 * This is where the users can decided what to listen to. This will also allow the user
 * to add custom tracks to their library
 */
@Composable
fun Library() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val popUpDialog = remember { mutableStateOf(false) }
    val customSounds = remember { mutableStateListOf<Sounds>()}


    LaunchedEffect(Unit) {
        val loaded = loadSounds(context, SOUNDS_FILE_PATH)
        customSounds.clear()
        customSounds.addAll(loaded)
        Log.d("TESTIN", "Hello $loaded")
    }

    Box(
        modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddNoise(popUpDialog.value, customSounds) { popUpDialog.value = false }

            Text(
                text = "Sound Library",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ShowSounds(defaultSounds, context)

            ShowSounds(customSounds, context)
        }

        OutlinedButton(
            onClick = { popUpDialog.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp),
        ) {
            val filepath = if (isSystemInDarkTheme()) "dark-add.svg" else "add.svg"
            SvgImageFromAssets(
                filepath = filepath,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun ShowSounds(sounds: List<Sounds>, context: Context) {
    sounds.forEach { sound ->
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .clickable {
                    AudioPlayer.playAsset(context, sound)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isSvg = sound.imagePath.endsWith(".svg", ignoreCase = true)

                if (isSvg) {
                    SvgImageFromAssets(
                        sound.imagePath,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(File(sound.imagePath)),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column {
                    Text(text = sound.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = sound.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
