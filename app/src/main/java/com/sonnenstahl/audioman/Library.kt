package com.sonnenstahl.audioman

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sonnenstahl.audioman.utils.*

/**
 * @brief this is where all of the sounds are stored with the default ones being immutable.
 * users can add their own ones by pressing the + button which will display AddNoise.kt. The user
 * can also delete and modify the custom sounds by swiping and long pressing them.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Library() {
    val context = LocalContext.current
    val popUpDialog = remember { mutableStateOf(false) }
    val customSounds = remember { mutableStateListOf<Noise>() }
    val currentNoise = remember { mutableStateOf<Noise?>(null) }

    LaunchedEffect(Unit) {
        val loaded = loadSounds(context, SOUNDS_FILE_PATH)
        customSounds.clear()
        customSounds.addAll(loaded)
    }

    AddNoise(
        showDialog = popUpDialog.value,
        soundsList = customSounds,
        currentSound = currentNoise,
    ) {
        currentNoise.value = null
        popUpDialog.value = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text(
                    text = "Sound Library",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            items(defaultSounds) { sound ->
                SoundItem(sound = sound, context = context, isCustom = false)
            }

            items(
                items = customSounds,
                key = { sound -> sound.id },
            ) { sound ->
                CustomSoundItem(
                    sound = sound,
                    soundsList = customSounds,
                    currentSound = currentNoise,
                    openDialogTrigger = popUpDialog,
                    context = context,
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        OutlinedButton(
            onClick = { popUpDialog.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp),
        ) {
            val filepath = if (isSystemInDarkTheme()) "dark-add.svg" else "add.svg"
            SvgImageFromAssets(
                filepath = filepath,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
            )
        }
    }
}
