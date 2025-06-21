package com.sonnenstahl.audioman

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
fun NoiseGenUI() {
    var noiseType by remember { mutableStateOf("White") }
    var amplitude by remember { mutableFloatStateOf(0.5f) }
    var spectrum by remember { mutableFloatStateOf(0.5f) }
    val previewSamples = remember { mutableStateOf<ByteArray>(ByteArray(size = 44100 * 2)) }
    val lineColor = remember { mutableStateOf(Color.White) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Noise Generator", style = MaterialTheme.typography.headlineMedium)

        FrequencyGraph(samples = previewSamples.value, lineColor = lineColor.value)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Noise Type: ")
            DropdownMenuBox(
                options = listOf("White", "Pink", "Brown"),
                selected = noiseType,
                onSelectedChange = { noiseType = it }
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
            "Spectrum (Brightness)",
            spectrum, 0f..1f,
            onChange = {  spectrum = it },
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

        Button(onClick = {
            val file = File(context.cacheDir, "generated_noise.wav")
            val sampleRate = 44100
            val durationSec = 1

            val samples = generateNoiseSamples(noiseType, amplitude, spectrum, sampleRate, durationSec)
            previewSamples.value = samples
            val path = writeWav(samples, sampleRate, file)
            val sound = Noise(
                "Generated Noise",
                "Noise generated via sliders",
                path
            )
            saveSound(context, sound, CURRENT_SOUND_PATH)

            AudioPlayer.playAsset(context, sound)
            Toast.makeText(context, "Saved to: $path", Toast.LENGTH_LONG).show()
        }) {
            Text("Generate & Save")
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
    Column {
        Text("$label: ${"%.2f".format(value)}")
        Slider(
            value = value,
            onValueChange = onChange,
            onValueChangeFinished = onFinalChange,
            valueRange = range
        )
    }
}

@Composable
fun DropdownMenuBox(options: List<String>, selected: String, onSelectedChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
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