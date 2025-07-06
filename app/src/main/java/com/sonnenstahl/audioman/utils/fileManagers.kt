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
 * @brief saves both images and audio files as uris to storage
 *
 * @param context of the current view
 * @param uri the location of the image to be stored
 * @param prefix this is set by me so that there is no claching in images
 *
 * @return String? the filepath if it was successful. If not, returns null
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

/**
 * @brief saves the custom sounds in the library to disk
 *
 * @param context context of the current view
 * @param sounds the custom sounds by the user to be saved
 * @param filepath the localtion in storage to be saved
 */
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

/**
 * @brief load all the custom sounds as from disk
 *
 * @param context context of the view
 * @param filepath should be the same as the one in which you used for saveSounds
 *
 * @return a mutable list of Sound. If the file was not found this list is empty
 */
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

/**
 * @brief save a singular
 */
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
