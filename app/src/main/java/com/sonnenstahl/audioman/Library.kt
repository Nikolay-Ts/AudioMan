package com.sonnenstahl.audioman

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import com.sonnenstahl.audioman.utils.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Library() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val popUpDialog = remember { mutableStateOf(false) }
    val customSounds = remember { mutableStateListOf<Noise>() }
    val currentNoise = remember { mutableStateOf<Noise?>(null) }
    val recomposeCounter = remember { mutableIntStateOf(0) }

    LaunchedEffect(recomposeCounter.intValue) {
        val loaded = loadSounds(context, SOUNDS_FILE_PATH)
        customSounds.clear()
        customSounds.addAll(loaded)
    }


    AddNoise(
        showDialog = popUpDialog.value,
        soundsList = customSounds,
        currentSound = currentNoise
    ) {
        currentNoise.value = null
        popUpDialog.value = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Sound Library",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ShowSounds(defaultSounds, context)

            ShowCustomSounds(
                sounds = customSounds,
                currentSound = currentNoise,
                count = recomposeCounter,
                openDialogTrigger = popUpDialog,
                context = context
            )
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