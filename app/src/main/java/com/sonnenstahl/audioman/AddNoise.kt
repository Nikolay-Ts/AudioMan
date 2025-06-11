package com.sonnenstahl.audioman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.sonnenstahl.audioman.utils.Sounds
import com.sonnenstahl.audioman.utils.saveUri

// TODO: Add the other fields, and make a picker for images and files
// TODO: Validation function and highlight in red what is not good
@Composable
fun AddNoise(showDialog: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current

    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val audioUri = remember { mutableStateOf<Uri?>(null) }
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
                    TextField(
                        value=title.value,
                        onValueChange = { title.value = it },
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .fillMaxWidth(),
                        enabled = true,
                        label = { Text("Title") },
                        placeholder = { Text("Title of you own Noise") }
                    )
                    TextField(
                        value=description.value,
                        onValueChange = { description.value = it },
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .fillMaxWidth(),
                        enabled = true,
                        label = { Text("Description") },
                        placeholder = { Text("What makes your Noise special") }
                    )

                    OutlinedButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(if (audioUri.value != null)
                            "Selected: ${audioUri.value?.lastPathSegment}"
                        else
                            "Pick Audio File")
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
                            "Pick Image")
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 30.dp,
                                vertical = 20.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val commonModifer = Modifier.width(50.dp)

                        OutlinedButton(
                            onClick = onDismiss
                        ) {
                            Box(
                                modifier = commonModifer,
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cancel", color = Color.Red, modifier = commonModifer)
                            }
                        }

                        Button(
                            colors= ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            onClick = {

                                val storedAudioPath = audioUri.value?.let {
                                    saveUri(context, it, "audio_${System.currentTimeMillis()}")
                                }

                                val storedImagePath = imageUri.value?.let {
                                    saveUri(context, it, "image_${System.currentTimeMillis()}")
                                }

                                val newSound = Sounds(
                                    title.value,
                                    description.value,
                                    storedAudioPath ?: "default.m4a",
                                    storedImagePath ?: "default.svg"
                                )



                                onDismiss
                            }
                        ) {
                            Box(
                                modifier = commonModifer,
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Add", modifier = commonModifer)
                            }
                        }

                    }
                }
            }

        }
    }
}