package com.sonnenstahl.audioman.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

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

fun saveSound(
    context: Context,
    sound: Sounds,
    filepath: String
): Nothing = TODO()