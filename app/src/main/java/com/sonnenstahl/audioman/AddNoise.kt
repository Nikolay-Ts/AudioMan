package com.sonnenstahl.audioman

import android.util.Log
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

const val PREFIX: String = "/data/user/0/com.sonnenstahl.audioman/files/"

@Composable
fun AddNoise(
    showDialog: Boolean,
    soundsList: SnapshotStateList<Noise>,
    currentSound: MutableState<Noise?>,
    onDismiss: () -> Unit,
) {
    Log.d("MEOW MEOW", "In the thingt ${currentSound.value}")
    val context         = LocalContext.current
    val keyboard        = LocalSoftwareKeyboardController.current
    val currentSoundVal = currentSound?.value
    val title           = remember { mutableStateOf(currentSound.value?.title ?: "title") }
    val description     = remember { mutableStateOf(currentSound.value?.description ?: "description") }
    val audioUri        = remember { mutableStateOf(currentSound.value?.audioPath?.toUri()) }
    val imageUri        = remember { mutableStateOf(currentSound.value?.imagePath?.toUri()) }

    val supportTitle = remember { mutableStateOf("") }
    val supportAudio = remember { mutableStateOf("") }
    val notUnique    = remember { mutableStateOf(false) }
    val validNoise   = remember { mutableStateOf(ValidNoise()) }

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
                            value = currentSound.value?.title ?: title.value,
                            onValueChange = {
                                title.value = it
                                currentSound.value?.title = it
                                if (!validNoise.value.title) {
                                    validNoise.value.title =
                                        soundsList.any { sound -> sound.title == title.value }
                                }
                            },
                            label = { "title" },
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
                        value = currentSoundVal?.description ?: "description",
                        onValueChange = { description.value = it },
                        label = { description.value },
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
                                    "Selected Track: ${audioUri.value?.lastPathSegment}"
                                else if (currentSound.value?.audioPath != null)
                                    "Selected Track: ${currentSound.value?.audioPath?.substring(PREFIX.length)}"
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
                            else if (currentSound.value?.imagePath != null) {
                                "Selected Image: ${currentSound.value?.imagePath?.substring(PREFIX.length)}"
                            }
                            else "Cover",
                            color = Color.White
                        )
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
                                audioPath = storedAudioPath ?: currentSoundVal?.audioPath ?: DEFAULT_AUDIO_URI,
                                imagePath = storedImagePath ?: currentSoundVal?.imagePath ?: DEFAULT_IMAGE_URI
                            )

                            validNoise.value = validateNoise(newNoise)

                            notUnique.value = soundsList.any {
                                it.title == newNoise.title && it != currentSoundVal
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

                            val index = soundsList.indexOfFirst { it.title == currentSoundVal?.title }

                            if (index != -1) {
                                soundsList[index] = newNoise
                            } else {
                                soundsList.add(newNoise)
                            }

                            saveSounds(context, soundsList, SOUNDS_FILE_PATH)
                            title.value = ""
                            description.value = ""
                            audioUri.value = null
                            imageUri.value = null
                            onDismiss()
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal)
                    ) {
                        val text = if (currentSound.value == null) "Add Noise" else "modify"
                        Text(text, color = Color.White)
                    }
                }
            }
        }
    }
}