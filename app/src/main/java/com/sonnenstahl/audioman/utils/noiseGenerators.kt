package com.sonnenstahl.audioman.utils

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

fun generateNoiseSamples(
    type: String,
    amplitude: Float,
    spectrum: Float,
    sampleRate: Int,
    durationSec: Int
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

        val sample = when (type.lowercase()) {
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

fun lerp(a: Double, b: Double, alpha: Float): Double = (1 - alpha) * a + alpha * b

fun writeWav(audioData: ByteArray, sampleRate: Int, outputFile: File): String {
    val header = createWavHeader(audioData.size, sampleRate)
    FileOutputStream(outputFile).use { fos ->
        fos.write(header)
        fos.write(audioData)
    }
    return outputFile.absolutePath
}

fun createWavHeader(dataLength: Int, sampleRate: Int): ByteArray {
    val totalDataLen = 36 + dataLength
    val byteRate = sampleRate * 2

    return ByteBuffer.allocate(44).apply {
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
