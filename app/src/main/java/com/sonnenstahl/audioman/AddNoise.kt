package com.sonnenstahl.audioman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.utils.DEFAULT_AUDIO_URI
import com.sonnenstahl.audioman.utils.DEFAULT_IMAGE_URI
import com.sonnenstahl.audioman.utils.SOUNDS_FILE_PATH
import com.sonnenstahl.audioman.utils.Noise
import com.sonnenstahl.audioman.utils.ValidNoise
import com.sonnenstahl.audioman.utils.saveSounds
import com.sonnenstahl.audioman.utils.saveUri
import com.sonnenstahl.audioman.utils.validateNoise

// TODO: Validation function and highlight in red what is not good
@Composable
fun AddNoise(
    showDialog: Boolean,
    soundsList: SnapshotStateList<Noise>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val title = remember { mutableStateOf("") }
    val supportTitle = remember { mutableStateOf("") } // for when the user gets it wrong
    val notUnique = remember { mutableStateOf(false) }
    val description = remember { mutableStateOf("") }
    val audioUri = remember { mutableStateOf<Uri?>(null) }
    val supportaudio = remember { mutableStateOf("") } // for when the user gets it wrong
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        audioUri.value = uri
    }
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }

    val validNoise = remember { mutableStateOf(ValidNoise()) }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Add a new Track!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        TextField(
                            value = title.value,
                            onValueChange = {
                                title.value = it
                                if (!validNoise.value.title) {
                                    validNoise.value.title =
                                        soundsList.any { it.title == title.value }
                                }
                                },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    "Title",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            },
                            placeholder = {
                                Text(
                                    "Title of your own Noise",
                                    modifier = Modifier.padding(start = 4.dp)

                                )
                            },
                            isError = ! validNoise.value.title,
                            singleLine = true
                        )
                        Text(
                            text = supportTitle.value,
                            color = Color.Red,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .align(Alignment.Start)
                        )
                    }

                    TextField(
                        value = description.value,
                        onValueChange = { description.value = it },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top=16.dp)
                            .fillMaxWidth(),
                        label = { Text("Description") },
                        placeholder = { Text("What makes your Noise special") },
                        singleLine = false
                    )

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (audioUri.value != null)
                                "Selected: ${audioUri.value?.lastPathSegment}"
                            else
                                "Track")
                        }
                        Text(
                            text = supportaudio.value,
                            color = Color.Red,
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp)
                                .align(Alignment.Start)
                        )
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(if (imageUri.value != null)
                            "Selected Image: ${imageUri.value?.lastPathSegment}"
                        else
                            "Cover")
                    }

                    imageUri.value?.let { uri ->
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            onClick = {
                                imageUri.value = null
                                return@Button
                            }
                        ) {
                            androidx.compose.foundation.Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .height(100.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        modifier = Modifier.padding(bottom = 20.dp),
                        onClick = {
                            val storedAudioPath = audioUri.value?.let {
                                saveUri(context, it, "audio_${System.currentTimeMillis()}")
                            }

                            val storedImagePath = imageUri.value?.let {
                                saveUri(context, it, "image_${System.currentTimeMillis()}")
                            }

                            val newNoise = Noise(
                                title.value,
                                description.value,
                                storedAudioPath ?: DEFAULT_AUDIO_URI,
                                storedImagePath ?: DEFAULT_IMAGE_URI
                            )

                            validNoise.value = validateNoise(newNoise)

                            notUnique.value =  soundsList.any { it.title == newNoise.title }

                            Log.d("MEOW MEOW", "path: ${validNoise.value.audioPath}\nmoew ${validNoise.value.title}\nempty: ${title.value}\n notUnique: $notUnique")

                            if (!validNoise.value.title) {
                                supportTitle.value = "Each Sound deserves a title"
                                return@Button
                            } else if (notUnique.value) {
                                supportTitle.value = "Title must be unique"
                                return@Button
                            }

                            if (!validNoise.value.audioPath) {
                                supportaudio.value = "choose your Track"
                                return@Button
                            }



                            soundsList.add(newNoise)
                            saveSounds(context, soundsList, SOUNDS_FILE_PATH)

                            // Reset form state
                            title.value = ""
                            description.value = ""
                            audioUri.value = null
                            imageUri.value = null

                            onDismiss()
                        }
                    ) {
                        Text("Add Noise")
                    }
                }
            }

        }
    }
}