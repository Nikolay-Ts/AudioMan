package com.sonnenstahl.audioman.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import kotlinx.serialization.encodeToString
import java.io.FileNotFoundException


const val SOUNDS_FILE_PATH: String = "sounds.json"

// TODO: add all of the serialisation of the data class Sounds
/**
 * because it is URI based, this works for both sound and images
 */
fun saveUri(
    context: Context,
    uri: Uri,
    prefix: String
): String? {
    return try {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType) ?: "bin"

        val fileName = "$prefix.${extension}"
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
}

fun saveSounds(
    context: Context,
    sounds: List<Sounds>,
    filepath: String
) {
    try {
        val json = Json.encodeToString(sounds)
        val file = File(context.filesDir, filepath)
        file.writeText(json)
    } catch (e: Exception) {
        Log.d("MEOW", e.toString())
    }
}

fun loadSounds(context: Context, filepath: String): List<Sounds> {
    val file = File(context.filesDir, filepath)
    return try {
        val jsonString = file.readText()
        Json.decodeFromString(jsonString)
    } catch (e: FileNotFoundException) {
        emptyList<Sounds>()
    }
}
