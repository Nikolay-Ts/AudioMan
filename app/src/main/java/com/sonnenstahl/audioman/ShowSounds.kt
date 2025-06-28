package com.sonnenstahl.audioman

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.ui.theme.LightTeal
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.CURRENT_SOUND_PATH
import com.sonnenstahl.audioman.utils.DEFAULT_IMAGE_URI
import com.sonnenstahl.audioman.utils.DEFAULT_LIGHT_IMAGE
import com.sonnenstahl.audioman.utils.Noise
import com.sonnenstahl.audioman.utils.saveSound
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundItem(sound: Noise, context: Context, isCustom: Boolean = false) {
    val darkMode = isSystemInDarkTheme()
    val borderColor = when (darkMode) {
        true -> Color.LightGray
        false -> Color.Black
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .background(LightTeal, shape = RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .combinedClickable {
                AudioPlayer.playAsset(context, sound)
                saveSound(context, sound, CURRENT_SOUND_PATH)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isCustom) {

                val imageModel = if (DEFAULT_LIGHT_IMAGE in sound.imagePath || DEFAULT_IMAGE_URI in sound.imagePath ) {
                    "file:///android_asset/$DEFAULT_LIGHT_IMAGE}"
                } else {
                    File(sound.imagePath).absolutePath
                }
                Image(
                    painter = rememberAsyncImagePainter(imageModel),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                val path = when (darkMode) {
                    true  -> DEFAULT_LIGHT_IMAGE
                    false -> DEFAULT_IMAGE_URI
                }
                Image(
                    rememberAsyncImagePainter(model = "file:///android_asset/$path"),
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