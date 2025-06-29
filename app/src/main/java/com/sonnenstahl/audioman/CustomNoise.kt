package com.sonnenstahl.audioman

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sonnenstahl.audioman.ui.theme.Brown
import com.sonnenstahl.audioman.ui.theme.DarkPlotBackGround
import com.sonnenstahl.audioman.ui.theme.LightBrown
import com.sonnenstahl.audioman.ui.theme.LightPlotBackGround
import com.sonnenstahl.audioman.ui.theme.LightTeal
import com.sonnenstahl.audioman.ui.theme.Pink40
import com.sonnenstahl.audioman.ui.theme.Pink80
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.CURRENT_SOUND_PATH
import com.sonnenstahl.audioman.utils.CUSTOM_SOUND_PATH
import com.sonnenstahl.audioman.utils.CustomNoise
import com.sonnenstahl.audioman.utils.Noise
import com.sonnenstahl.audioman.utils.generateNoiseSamples
import com.sonnenstahl.audioman.utils.loadCustomSound
import com.sonnenstahl.audioman.utils.saveCustomSound
import com.sonnenstahl.audioman.utils.saveSound
import com.sonnenstahl.audioman.utils.updateGraphData
import com.sonnenstahl.audioman.utils.writeWav
import kotlinx.coroutines.launch
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomNoise() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()

    var customNoiseData by remember {
        mutableStateOf(loadCustomSound(context, CUSTOM_SOUND_PATH) ?: CustomNoise("White", 0.5f, 0.5f, ByteArray(size = 44100 * 2)))
    }
    var noiseType by remember { mutableStateOf(customNoiseData.noiseType) }
    var amplitude by rememberSaveable { mutableFloatStateOf(customNoiseData.amplitude) }
    var spectrum by rememberSaveable { mutableFloatStateOf(customNoiseData.spectrum) }
    val previewSamples = remember { mutableStateOf(customNoiseData.samples) }
    val isPlaying = remember { mutableStateOf(AudioPlayer.isPlaying()) }
    val currentSound = remember { mutableStateOf(AudioPlayer.getSound()) }
    val targetLineColor =
        remember(noiseType, isDarkTheme) {
            mutableStateOf(
                when (noiseType) {
                    "White" -> if (isDarkTheme) Color.LightGray else Color.Gray
                    "Pink" -> if (isDarkTheme) Pink80 else Pink40
                    "Brown" -> if (isDarkTheme) LightBrown else Brown
                    else -> Color.Red
                },
            )
        }

    LaunchedEffect(noiseType, amplitude, spectrum) {
        val newSamples =
            generateNoiseSamples(
                noiseType,
                amplitude,
                spectrum,
                44100,
                1,
            )
        previewSamples.value = newSamples
        customNoiseData =
            customNoiseData.copy(
                noiseType = noiseType,
                amplitude = amplitude,
                spectrum = spectrum,
                samples = newSamples,
            )
        updateGraphData(
            context,
            noiseType,
            amplitude,
            spectrum,
            previewSamples,
            targetLineColor,
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Noise Generator",
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.padding(bottom = 24.dp),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            FrequencyGraph(
                samples = previewSamples.value,
                lineColor = targetLineColor,
                backgroundColor = if (isDarkTheme) LightPlotBackGround else DarkPlotBackGround,
                gridColor = if (isDarkTheme) DarkPlotBackGround else LightPlotBackGround,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Noise Type:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                DropdownMenuBox(
                    options = listOf("White", "Pink", "Brown"),
                    selected = noiseType,
                    onSelectedChange = {
                        noiseType = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SliderWithLabel(
                "Amplitude",
                amplitude,
                0f..1f,
                onChange = { amplitude = it },
                onFinalChange = { },
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderWithLabel(
                "Brightness",
                spectrum,
                0f..1f,
                onChange = { spectrum = it },
                onFinalChange = { },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // This is the ONLY place where playback should be initiated or paused
        AnimatedPause(
            isPlaying = isPlaying.value,
            size = 200,
            modifier =
                Modifier
                    .size(100.dp)
                    .padding(16.dp),
        ) {
            coroutineScope.launch {
                if (currentSound.value.id == "-2" && isPlaying.value) {
                    AudioPlayer.pause()
                    isPlaying.value = false
                } else if (isPlaying.value == true) {
                    AudioPlayer.pause()
                    isPlaying.value = false
                } else {
                    isPlaying.value = true
                    val file = File(context.cacheDir, "generated_noise.wav")
                    val sampleRate = 44100
                    val durationSec = 1

                    val newSamples =
                        generateNoiseSamples(
                            noiseType,
                            amplitude,
                            spectrum,
                            sampleRate,
                            durationSec,
                        )
                    previewSamples.value = newSamples

                    val path = writeWav(newSamples, sampleRate, file)

                    val generatedNoise =
                        Noise(
                            "-2",
                            "Generated Noise",
                            "Noise generated via sliders",
                            path,
                        )

                    val updatedCustomNoise =
                        CustomNoise(
                            noiseType,
                            amplitude,
                            spectrum,
                            newSamples,
                        )
                    saveCustomSound(context, updatedCustomNoise, CUSTOM_SOUND_PATH)
                    saveSound(context, generatedNoise, CURRENT_SOUND_PATH)

                    AudioPlayer.initialize(context)
                    AudioPlayer.playAsset(context, generatedNoise)
                }
            }
        }
    }
}

@Composable
fun SliderWithLabel(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    onFinalChange: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label: ${"%.2f".format(value)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp),
        )
        Slider(
            value = value,
            onValueChange = onChange,
            onValueChangeFinished = onFinalChange,
            valueRange = range,
            colors =
                SliderDefaults.colors(
                    thumbColor = Teal,
                    activeTrackColor = LightTeal,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DropdownMenuBox(
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = LightTeal),
            onClick = { expanded = true },
            modifier = Modifier.height(48.dp),
        ) {
            Text(selected, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onSelectedChange(it)
                        expanded = false
                    },
                )
            }
        }
    }
}
