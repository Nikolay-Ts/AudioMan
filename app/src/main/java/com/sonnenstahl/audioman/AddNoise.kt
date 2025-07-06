package com.sonnenstahl.audioman

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.glance.appwidget.updateAll
import com.sonnenstahl.audioman.ui.theme.Teal
import com.sonnenstahl.audioman.utils.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AddNoise(
    showDialog: Boolean,
    soundsList: SnapshotStateList<Noise>,
    currentSound: MutableState<Noise?>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    // Initialize title and description with default values if currentSound is null
    val coroutinScope = rememberCoroutineScope()
    val title = remember { mutableStateOf(currentSound.value?.title ?: "") }
    val description = remember { mutableStateOf(currentSound.value?.description ?: "") }
    val audioUri =
        remember {
            mutableStateOf(
                currentSound.value?.audioPath?.let { path ->
                    if (path.startsWith("android_asset/")) path.toUri() else File(path).toUri()
                },
            )
        }
    val imageUri =
        remember {
            mutableStateOf(
                currentSound.value?.imagePath?.let { path ->
                    if (path.startsWith("android_asset/")) path.toUri() else File(path).toUri()
                },
            )
        }

    val darkMode = isSystemInDarkTheme()
    val supportTitle = remember { mutableStateOf("") }
    val supportAudio = remember { mutableStateOf("") }
    val notUnique = remember { mutableStateOf(false) }
    val validNoise = remember { mutableStateOf(ValidNoise()) }

    val audioPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri -> audioUri.value = uri }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri -> imageUri.value = uri }

    LaunchedEffect(currentSound.value) {
        title.value = currentSound.value?.title ?: ""
        description.value = currentSound.value?.description ?: ""
        audioUri.value =
            currentSound.value?.audioPath?.let { path ->
                if (path.startsWith("android_asset/")) path.toUri() else File(path).toUri()
            }
        imageUri.value =
            currentSound.value?.imagePath?.let { path ->
                if (path.startsWith("android_asset/")) path.toUri() else File(path).toUri()
            }
        supportTitle.value = ""
        supportAudio.value = ""
        notUnique.value = false
        validNoise.value = ValidNoise()
    }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                        .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Add a new Track!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 25.dp),
                    )

                    // Title input field
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = title.value,
                            onValueChange = {
                                title.value = it
                                supportTitle.value = ""
                                notUnique.value = false
                                validNoise.value = validNoise.value.copy(title = true)
                            },
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            label = { Text("Title") },
                            placeholder = { Text("Enter a title for your Noise") },
                            isError = !validNoise.value.title || notUnique.value,
                            singleLine = true,
                            colors =
                                TextFieldDefaults.colors(
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    errorTextColor = MaterialTheme.colorScheme.error,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                        )
                        if (!validNoise.value.title || notUnique.value) {
                            Text(
                                text = supportTitle.value.ifEmpty { "Title must be unique and not empty" },
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp),
                            )
                        }
                    }

                    // Description input field
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val focusManager = LocalFocusManager.current
                        TextField(
                            value = description.value,
                            onValueChange = { description.value = it },
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            label = { Text("Description") },
                            placeholder = { Text("Describe what makes your Noise special") },
                            singleLine = false,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            colors =
                                TextFieldDefaults.colors(
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                ),
                        )
                    }

                    // Audio Picker
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedButton(
                            onClick = {
                                keyboard?.hide()
                                audioPickerLauncher.launch("audio/*")
                                supportAudio.value = ""
                                validNoise.value = validNoise.value.copy(audioPath = true)
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                        ) {
                            Text(
                                if (audioUri.value != null) {
                                    "Selected Track: ${audioUri.value?.lastPathSegment ?: "..."}"
                                } else if (currentSound.value?.audioPath != null &&
                                    !currentSound.value!!.audioPath.startsWith("android_asset/")
                                ) {
                                    "Selected Track: ${File(currentSound.value!!.audioPath).name}"
                                } else if (currentSound.value?.audioPath != null &&
                                    currentSound.value!!.audioPath.startsWith("android_asset/")
                                ) {
                                    "Selected Track: Default Asset"
                                } else {
                                    "Select Track"
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (!validNoise.value.audioPath && supportAudio.value.isNotEmpty()) {
                            Text(
                                text = supportAudio.value,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp),
                            )
                        }
                    }

                    // Image Picker
                    OutlinedButton(
                        onClick = {
                            keyboard?.hide()
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            if (imageUri.value != null) {
                                "Selected Image: ${imageUri.value?.lastPathSegment ?: "..."}"
                            } else if (currentSound.value?.imagePath != null &&
                                !currentSound.value!!.imagePath.startsWith("android_asset/")
                            ) {
                                "Selected Image: ${File(currentSound.value!!.imagePath).name}"
                            } else if (currentSound.value?.imagePath != null &&
                                currentSound.value!!.imagePath.startsWith("android_asset/")
                            ) {
                                "Selected Image: Default Asset"
                            } else {
                                "Select Cover"
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Button(
                        onClick = {
                            val storedAudioPath =
                                audioUri.value?.let {
                                    saveUri(context, it, "audio_${System.currentTimeMillis()}")
                                }

                            val storedImagePath =
                                imageUri.value?.let {
                                    saveUri(context, it, "image_${System.currentTimeMillis()}")
                                }

                            val finalImagePath: String
                            if (storedImagePath != null) {
                                finalImagePath = storedImagePath
                            } else if (currentSound.value?.imagePath != null &&
                                !currentSound.value!!.imagePath.startsWith("android_asset/")
                            ) {
                                finalImagePath = currentSound.value!!.imagePath
                            } else {
                                val defaultAssetFilename = if (darkMode) DEFAULT_LIGHT_IMAGE else DEFAULT_IMAGE_URI
                                finalImagePath = "android_asset/$defaultAssetFilename"
                            }

                            val finalAudioPath: String
                            if (storedAudioPath != null) {
                                finalAudioPath = storedAudioPath
                            } else if (currentSound.value?.audioPath != null &&
                                !currentSound.value!!.audioPath.startsWith("android_asset/")
                            ) {
                                finalAudioPath = currentSound.value!!.audioPath
                            } else {
                                val defaultAssetAudioFilename = DEFAULT_AUDIO_URI
                                finalAudioPath = "android_asset/$defaultAssetAudioFilename"
                            }

                            val newNoise =
                                Noise(
                                    id =
                                        currentSound.value?.id ?: java.util.UUID
                                            .randomUUID()
                                            .toString(),
                                    title = title.value,
                                    description = description.value,
                                    audioPath = finalAudioPath,
                                    imagePath = finalImagePath,
                                )

                            validNoise.value = validateNoise(newNoise)

                            notUnique.value =
                                soundsList.any {
                                    it.title == newNoise.title && it.id != newNoise.id
                                }

                            if (title.value.isBlank()) {
                                validNoise.value = validNoise.value.copy(title = false)
                                supportTitle.value = "Each Sound deserves a title"
                            } else if (notUnique.value) {
                                validNoise.value = validNoise.value.copy(title = false)
                                supportTitle.value = "Title must be unique"
                            } else {
                                supportTitle.value = ""
                            }

                            if (newNoise.audioPath.startsWith("android_asset/") && audioUri.value == null && currentSound.value == null) {
                                validNoise.value = validNoise.value.copy(audioPath = false)
                                supportAudio.value = "Choose your Track"
                            } else if (newNoise.audioPath.isBlank() || newNoise.audioPath == "android_asset/") {
                                validNoise.value = validNoise.value.copy(audioPath = false)
                                supportAudio.value = "Choose your Track"
                            } else {
                                supportAudio.value = ""
                            }

                            if (!validNoise.value.title || !validNoise.value.audioPath || notUnique.value) {
                                return@Button
                            }

                            val index = soundsList.indexOfFirst { it.id == newNoise.id }
                            if (index != -1) {
                                soundsList[index] = newNoise
                            } else {
                                soundsList.add(newNoise)
                            }

                            saveSounds(context, soundsList, SOUNDS_FILE_PATH)
                            coroutinScope.launch {
                                HomeWidget().updateAll(context)
                            }

                            // reset fields for next use
                            title.value = ""
                            description.value = ""
                            audioUri.value = null
                            imageUri.value = null
                            onDismiss()
                        },
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    ) {
                        val text = if (currentSound.value == null) "Add Noise" else "Modify"
                        Text(text, color = Color.White)
                    }
                }
            }
        }
    }
}
