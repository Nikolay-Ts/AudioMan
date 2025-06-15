package com.sonnenstahl.audioman

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.Noise
import com.sonnenstahl.audioman.utils.SOUNDS_FILE_PATH
import com.sonnenstahl.audioman.utils.saveSounds
import java.io.File
import kotlin.collections.forEach

@Composable
fun ShowSounds(sounds: List<Noise>, context: Context) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowCustomSounds(sounds: SnapshotStateList<Noise>, count: MutableState<Int>, context: Context) {
    sounds.forEach { sound ->
        val dismissState = rememberDismissState(
            confirmStateChange = {
                if (it == DismissValue.DismissedToStart) {
                    sounds.remove(sound)
                    count.value++
                    saveSounds(context, sounds, SOUNDS_FILE_PATH)
                    true
                } else false
            }
        )

        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart),
            dismissContent = {
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
            },
            background = {}
        )
    }

}