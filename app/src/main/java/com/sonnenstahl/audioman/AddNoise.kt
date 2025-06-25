package com.sonnenstahl.audioman

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.*

@Composable
fun AddNoise(
    showDialog: Boolean,
    soundsList: SnapshotStateList<Noise>,
    currentSound: Noise?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current

    val title = remember { mutableStateOf(currentSound?.title ?: "") }
    val description = remember { mutableStateOf(currentSound?.description ?: "") }
    val audioUri = remember { mutableStateOf(currentSound?.audioPath?.toUri()) }
    val imageUri = remember { mutableStateOf(currentSound?.imagePath?.toUri()) }

    val supportTitle = remember { mutableStateOf("") }
    val supportAudio = remember { mutableStateOf("") }
    val notUnique = remember { mutableStateOf(false) }
    val validNoise = remember { mutableStateOf(ValidNoise()) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> audioUri.value = uri }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri.value = uri }

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
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Add a new Track!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 25.dp)
                    )

                    Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                        TextField(
                            value = title.value,
                            onValueChange = {
                                title.value = it
                                if (!validNoise.value.title) {
                                    validNoise.value.title =
                                        soundsList.any { sound -> sound.title == title.value }
                                }
                            },
                            label = { Text("Title") },
                            placeholder = { Text("Title of your own Noise") },
                            isError = !validNoise.value.title,
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                unfocusedLabelColor = Color.White,
                                focusedTextColor = Color.White,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = supportTitle.value,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    TextField(
                        value = description.value,
                        onValueChange = { description.value = it },
                        label = { Text("Description") },
                        placeholder = { Text("What makes your Noise special") },
                        singleLine = false,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedLabelColor = Color.White,
                            focusedTextColor = Color.White,
                        )
                    )

                    Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                keyboard?.hide()
                                audioPickerLauncher.launch("audio/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (audioUri.value != null)
                                    "Selected: ${audioUri.value?.lastPathSegment}"
                                else "Track",
                                color = Color.White
                            )
                        }
                        Text(
                            text = supportAudio.value,
                            color = Color.Red,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            keyboard?.hide()
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            if (imageUri.value != null)
                                "Selected Image: ${imageUri.value?.lastPathSegment}"
                            else "Cover",
                            color = Color.White
                        )
                    }

                    imageUri.value?.let { uri ->
                        Button(
                            onClick = { imageUri.value = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Image(
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
                        onClick = {
                            val storedAudioPath = audioUri.value?.let {
                                saveUri(context, it, "audio_${System.currentTimeMillis()}")
                            }

                            val storedImagePath = imageUri.value?.let {
                                saveUri(context, it, "image_${System.currentTimeMillis()}")
                            }

                            val newNoise = Noise(
                                title = title.value,
                                description = description.value,
                                audioPath = storedAudioPath ?: currentSound?.audioPath ?: DEFAULT_AUDIO_URI,
                                imagePath = storedImagePath ?: currentSound?.imagePath ?: DEFAULT_IMAGE_URI
                            )

                            validNoise.value = validateNoise(newNoise)

                            notUnique.value = soundsList.any {
                                it.title == newNoise.title && it != currentSound
                            }

                            if (!validNoise.value.title) {
                                supportTitle.value = "Each Sound deserves a title"
                                return@Button
                            } else if (notUnique.value) {
                                supportTitle.value = "Title must be unique"
                                return@Button
                            }

                            if (!validNoise.value.audioPath) {
                                supportAudio.value = "Choose your Track"
                                return@Button
                            }

                            val index = soundsList.indexOfFirst { it.title == currentSound?.title }

                            if (index != -1) {
                                soundsList[index] = newNoise
                            } else {
                                soundsList.add(newNoise)
                                // Clear form only if adding
                                title.value = ""
                                description.value = ""
                                audioUri.value = null
                                imageUri.value = null
                            }

                            saveSounds(context, soundsList, SOUNDS_FILE_PATH)
                            onDismiss()
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal)
                    ) {
                        Text("Add Noise", color = Color.White)
                    }
                }
            }
        }
    }
}