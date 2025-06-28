package com.sonnenstahl.audioman

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.Icon
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.CURRENT_SOUND_PATH
import com.sonnenstahl.audioman.utils.DEFAULT_IMAGE_URI
import com.sonnenstahl.audioman.utils.DEFAULT_LIGHT_IMAGE
import com.sonnenstahl.audioman.utils.Noise
import com.sonnenstahl.audioman.utils.SOUNDS_FILE_PATH
import com.sonnenstahl.audioman.utils.fallBackSound
import com.sonnenstahl.audioman.utils.saveSound
import com.sonnenstahl.audioman.utils.saveSounds
import java.io.File

@androidx.annotation.OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun CustomSoundItem(
    sound: Noise,
    soundsList: SnapshotStateList<Noise>,
    currentSound: MutableState<Noise?>,
    onSoundRemoved: () -> Unit,
    openDialogTrigger: MutableState<Boolean>,
    context: Context
) {
    val darkMode = isSystemInDarkTheme()
    val borderColor = when (darkMode) {
        true -> Color.LightGray
        false -> Color.Black
    }

    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                val isCurrentlyPlayingDismissed = sound.id == AudioPlayer.getSound().id

                if (isCurrentlyPlayingDismissed) {
                    AudioPlayer.pause()
                    AudioPlayer.clearSound()
                    saveSound(context, fallBackSound, CURRENT_SOUND_PATH)
                }

                soundsList.remove(sound)
                onSoundRemoved()
                saveSounds(context, soundsList, SOUNDS_FILE_PATH)

                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                } else {
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
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
                    .background(Teal, shape = RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
                    .combinedClickable(
                        onClick = {
                            AudioPlayer.playAsset(context, sound)
                            saveSound(context, sound, CURRENT_SOUND_PATH)
                        },
                        onLongClick = {
                            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                            } else {
                                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            }
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    150,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )

                            currentSound.value = sound
                            openDialogTrigger.value = true
                            Log.d("MEOW MEOW", "${currentSound.value}")
                        }
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val imageModel = when (sound.imagePath) {
                        "android_asset/default_white.png" -> "file:///android_asset/$DEFAULT_LIGHT_IMAGE"
                        "android_asset/default_white.png" -> "file:///android_asset/$DEFAULT_IMAGE_URI"
                        else -> File(sound.imagePath).absolutePath
                    }

                    Image(
                        painter = rememberAsyncImagePainter(model = imageModel),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )

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
        background = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(72.dp)
                    .padding(horizontal = 12.dp)
                    .background(Color.Red, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(32.dp)
                )
            }
        }
    )
}