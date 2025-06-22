package com.sonnenstahl.audioman

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sonnenstahl.audioman.ui.theme.LightTeal
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.AudioPlayer
import com.sonnenstahl.audioman.utils.CURRENT_SOUND_PATH
import com.sonnenstahl.audioman.utils.Noise
import com.sonnenstahl.audioman.utils.generateNoiseSamples
import com.sonnenstahl.audioman.utils.saveSound
import com.sonnenstahl.audioman.utils.updateGraphData
import com.sonnenstahl.audioman.utils.writeWav
import java.io.File


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomNoise() {
    var noiseType by remember { mutableStateOf("White") }
    var amplitude by remember { mutableFloatStateOf(0.5f) }
    var spectrum by remember { mutableFloatStateOf(0.5f) }
    val isPlaying = remember { mutableStateOf(AudioPlayer.isPlaying()) }
    val previewSamples = remember { mutableStateOf<ByteArray>(ByteArray(size = 44100 * 2)) }
    val lineColor = remember { mutableStateOf(Color.Red) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Noise Generator", style = MaterialTheme.typography.headlineMedium)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            FrequencyGraph(samples = previewSamples.value, lineColor = lineColor)
            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text("Noise Type: ")
                DropdownMenuBox(
                    options = listOf("White", "Pink", "Brown"),
                    selected = noiseType,
                    onSelectedChange = {
                        noiseType = it
                        updateGraphData(
                            context,
                            noiseType,
                            amplitude,
                            spectrum,
                            previewSamples,
                            lineColor
                        )
                    }
                )
            }

            SliderWithLabel(
                "Amplitude", amplitude, 0f..1f,
                onChange = { amplitude = it },
                onFinalChange = {
                    updateGraphData(
                        context,
                        noiseType,
                        amplitude,
                        spectrum,
                        previewSamples,
                        lineColor
                    )
                }
            )
            SliderWithLabel(
                "Brightness",
                spectrum, 0f..1f,
                onChange = { spectrum = it },
                onFinalChange = {
                    updateGraphData(
                        context,
                        noiseType,
                        amplitude,
                        spectrum,
                        previewSamples,
                        lineColor
                    )
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedPause(isPlaying.value) {
                when (isPlaying.value) {
                    true -> {
                        AudioPlayer.pause()
                        isPlaying.value = false
                    }

                    false -> {
                        val file = File(context.cacheDir, "generated_noise.wav")
                        val sampleRate = 44100
                        val durationSec = 1

                        val samples =
                            generateNoiseSamples(
                                noiseType,
                                amplitude,
                                spectrum,
                                sampleRate,
                                durationSec
                            )
                        previewSamples.value = samples
                        val path = writeWav(samples, sampleRate, file)
                        val sound = Noise(
                            "Generated Noise",
                            "Noise generated via sliders",
                            path
                        )
                        saveSound(context, sound, CURRENT_SOUND_PATH)

                        AudioPlayer.playAsset(context, sound)

                        AudioPlayer.play()
                        isPlaying.value = true
                    }
                }
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
    onFinalChange: () -> Unit
) {
    Column(modifier = Modifier.padding(15.dp)) {
        Text(
            "$label: ${"%.2f".format(value)}",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 5.dp)
        )
        Slider(
            value = value,
            onValueChange = onChange,
            onValueChangeFinished = onFinalChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Teal,
                activeTrackColor = LightTeal,
                inactiveTrackColor = Color.White
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DropdownMenuBox(options: List<String>, selected: String, onSelectedChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            colors =  ButtonDefaults.buttonColors(Teal),
            onClick = { expanded = true }) {
            Text(selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelectedChange(it)
                    expanded = false
                })
            }
        }
    }
}