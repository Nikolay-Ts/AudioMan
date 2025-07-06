package com.sonnenstahl.audioman.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * because it is URI based, this works for both sound and images
 */
fun saveUri(
    context: Context,
    uri: Uri,
    prefix: String,
): String? =
    try {
        val mimeType = context.contentResolver.getType(uri)
        val extension =
            MimeTypeMap
                .getSingleton()
                .getExtensionFromMimeType(mimeType) ?: "bin"

        val fileName = "$prefix.$extension"
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

fun saveSounds(
    context: Context,
    sounds: List<Noise>,
    filepath: String,
) {
    try {
        val json = Json.encodeToString(sounds)
        val file = File(context.filesDir, filepath)
        file.writeText(json)
    } catch (e: Exception) {
    }
}

fun loadSounds(
    context: Context,
    filepath: String,
): MutableList<Noise> {
    val file = File(context.filesDir, filepath)
    return try {
        val jsonString = file.readText()
        Json.decodeFromString<List<Noise>>(jsonString).toMutableList()
    } catch (e: FileNotFoundException) {
        mutableStateListOf<Noise>()
    }
}

fun saveSound(
    context: Context,
    sounds: Noise,
    filepath: String,
) {
    try {
        val json = Json.encodeToString(sounds)
        val file = File(context.filesDir, filepath)
        file.writeText(json)
    } catch (e: Exception) {
    }
}

fun loadSound(
    context: Context,
    filepath: String,
): Noise? {
    val file = File(context.filesDir, filepath)
    return try {
        val jsonString = file.readText()
        Json.decodeFromString<Noise>(jsonString)
    } catch (e: FileNotFoundException) {
        null
    }
}

fun saveCustomSound(
    context: Context,
    sounds: CustomNoise,
    filepath: String,
) {
    try {
        val json = Json.encodeToString(sounds)
        val file = File(context.filesDir, filepath)
        file.writeText(json)
    } catch (e: Exception) {
    }
}

fun loadCustomSound(
    context: Context,
    filepath: String,
): CustomNoise? {
    val file = File(context.filesDir, filepath)
    if (!file.exists()) return null

    return try {
        val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        val content = file.readText()
        json.decodeFromString<CustomNoise>(content)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * for debugging purposes
 */
fun deleteSoundsFile(
    context: Context,
    filepath: String = SOUNDS_FILE_PATH,
): Boolean {
    val file = File(context.filesDir, filepath)
    return if (file.exists()) {
        file.delete().also {
            Log.d("FILE_DELETE", "Deleted $filepath: $it")
        }
    } else {
        Log.d("FILE_DELETE", "$filepath does not exist.")
        false
    }
}
