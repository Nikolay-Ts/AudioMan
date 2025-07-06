package com.sonnenstahl.audioman.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import com.sonnenstahl.audioman.ui.theme.Brown
import com.sonnenstahl.audioman.ui.theme.Pink40
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

/**
 * @brief generates the 'white' noise to be saved to disk and played
 *
 * @param type which is either White, Pink or Brown
 * @param amplitude how loud should the noise be
 * @param spectrum the frequency
 * @param sampleRate 44100
 * @param durationSec how long should it be. You can chose for it to be longer but 1s should
 * be enough as it loops and otherwise it would be very costly to generate the sound
 *
 * @return custom noise as a ByteArray
 */
fun generateNoiseSamples(
    type: String,
    amplitude: Float,
    spectrum: Float,
    sampleRate: Int,
    durationSec: Int,
): ByteArray {
    val numSamples = sampleRate * durationSec
    val output = ByteArray(numSamples * 2)
    var brown = 0.0
    val pinkHistory = DoubleArray(5)

    for (i in 0 until numSamples) {
        val white = Random.nextDouble(-1.0, 1.0)
        pinkHistory[i % 5] = white
        val pink = pinkHistory.average()
        brown += white * 0.02
        brown = brown.coerceIn(-1.0, 1.0)

        val sample =
            when (type.lowercase()) {
                "white" -> white
                "pink" -> lerp(white, pink, spectrum)
                "brown" -> lerp(pink, brown, spectrum)
                else -> white
            }

        val scaled = (sample * amplitude * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        output[i * 2] = (scaled and 0xFF).toByte()
        output[i * 2 + 1] = ((scaled shr 8) and 0xFF).toByte()
    }

    return output
}

fun lerp(
    a: Double,
    b: Double,
    alpha: Float,
): Double = (1 - alpha) * a + alpha * b

fun writeWav(
    audioData: ByteArray,
    sampleRate: Int,
    outputFile: File,
): String {
    val header = createWavHeader(audioData.size, sampleRate)
    FileOutputStream(outputFile).use { fos ->
        fos.write(header)
        fos.write(audioData)
    }
    return outputFile.absolutePath
}

/**
 * @brief creates the file header so that the file can be saved as .wav
 *
 * @param dataLength how long is the file in seconds
 * @param sampleRate the sample rate which is (44100) usually
 *
 * @return the header in bytes
 */
fun createWavHeader(
    dataLength: Int,
    sampleRate: Int,
): ByteArray {
    val totalDataLen = 36 + dataLength
    val byteRate = sampleRate * 2

    return ByteBuffer
        .allocate(44)
        .apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(sampleRate)
            putInt(byteRate)
            putShort(2)
            putShort(16)
            put("data".toByteArray())
            putInt(dataLength)
        }.array()
}

/**
 * @brief updates the custom sound and saves it to disk so that FrequencyGraph can
 * re-render the new sound wave
 *
 * @param context of the current view
 * @param noiseType White, Pink or Brown
 * @param amplitude of the wave
 * @param spectrum of the wave
 * @param samplesState
 * @param lineColor how to draw the lines
 */
@RequiresApi(Build.VERSION_CODES.O)
fun updateGraphData(
    context: Context,
    noiseType: String,
    amplitude: Float,
    spectrum: Float,
    samplesState: MutableState<ByteArray>,
    lineColor: MutableState<Color>,
) {
    if (!AudioPlayer.isPlaying()) return
    val sampleRate = 44100
    val durationSec = 1

    samplesState.value = generateNoiseSamples(noiseType, amplitude, spectrum, sampleRate, durationSec)

    lineColor.value =
        when (noiseType) {
            "White" -> Color.Gray
            "Pink" -> Pink40
            "Brown" -> Brown
            else -> Color.White
        }

    val customNoise =
        CustomNoise(
            noiseType,
            amplitude,
            spectrum,
            samplesState.value,
        )

    saveCustomSound(context, customNoise, CUSTOM_SOUND_PATH)
}
