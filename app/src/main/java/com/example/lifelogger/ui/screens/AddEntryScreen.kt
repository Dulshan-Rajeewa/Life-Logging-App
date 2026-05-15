package com.example.lifelogger.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.lifelogger.data.LifeLogEntry
import com.example.lifelogger.ui.LifeLogViewModel
import com.example.lifelogger.ui.components.*
import java.io.File

fun createImageUri(context: Context): Uri {
    val imageFolder = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imageFolder, "IMG_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    logId: String? = null,
    viewModel: LifeLogViewModel,
    userId: String?,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Category State
    var category by remember { mutableStateOf("General") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("General", "Study", "Workout", "Events", "Reflections", "Travel", "Work")

    // Camera States
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var temporaryUri by remember { mutableStateOf<Uri?>(null) }

    // Audio States
    var isRecording by remember { mutableStateOf(false) }
    var recordedAudioPath by remember { mutableStateOf<String?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    // Load existing data if editing
    LaunchedEffect(logId) {
        if (logId != null) {
            val existing = viewModel.getLogById(logId)
            if (existing != null) {
                title = existing.title
                description = existing.description
                category = existing.category
                if (existing.imageFilePath != null) {
                    capturedImageUri = Uri.parse(existing.imageFilePath)
                }
                recordedAudioPath = existing.audioFilePath
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) { capturedImageUri = temporaryUri }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) { capturedImageUri = uri }
    }

    // This launcher asks the user for microphone access the first time they click
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // In a production app you would show an error message here
        }
    }

    Scaffold(
        containerColor = LightGrayBg,
        topBar = {
            TopAppBar(
                title = { Text(if (logId != null) "Edit Entry" else "New Entry", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Entry Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealGreen, focusedLabelColor = TealGreen)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORY SELECTOR
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealGreen, focusedLabelColor = TealGreen)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("How was your day?") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealGreen, focusedLabelColor = TealGreen)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Attachments", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                // CAMERA BUTTON
                Button(
                    onClick = {
                        val uri = createImageUri(context)
                        temporaryUri = uri
                        cameraLauncher.launch(uri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Add Photo", tint = TealGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Camera", color = TextDark, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // GALLERY BUTTON
                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (capturedImageUri != null) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Photo Added", tint = TealGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Selected", color = TealGreen, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Gallery", tint = TealGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gallery", color = TextDark, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // AUDIO BUTTON
                Button(
                    onClick = {
                        // 1. Check if we have permission to use the microphone
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            // 2. If we are already recording, stop and save it
                            if (isRecording) {
                                try {
                                    mediaRecorder?.stop()
                                } catch (e: Exception) {
                                    // Handle cases where stop() is called before start() completes
                                } finally {
                                    mediaRecorder?.release()
                                    mediaRecorder = null
                                    isRecording = false
                                }
                            } else {
                                // 3. If we aren't recording, create a file and start listening!
                                val audioFile = File(context.cacheDir, "AUDIO_${System.currentTimeMillis()}.3gp")
                                recordedAudioPath = audioFile.absolutePath

                                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    MediaRecorder(context)
                                } else {
                                    @Suppress("DEPRECATION")
                                    MediaRecorder()
                                }

                                try {
                                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                    recorder.setOutputFile(audioFile.absolutePath)
                                    recorder.prepare()
                                    recorder.start()

                                    mediaRecorder = recorder
                                    isRecording = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isRecording) {
                            Icon(Icons.Default.Mic, contentDescription = "Recording", tint = Color.Red)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Recording...", color = Color.Red)
                        } else if (recordedAudioPath != null) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Audio Added", tint = TealGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Audio Saved", color = TealGreen)
                        } else {
                            Icon(Icons.Default.Mic, contentDescription = "Add Audio", tint = TealGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Voice Note", color = TextDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    var finalAudioPath = recordedAudioPath
                    
                    // STOP RECORDING IF ACTIVE
                    if (isRecording) {
                        try {
                            mediaRecorder?.stop()
                        } catch (e: Exception) {
                            android.util.Log.e("AddEntryScreen", "Stop recording failed during save", e)
                            finalAudioPath = null // Don't save if it failed (likely too short)
                        } finally {
                            mediaRecorder?.release()
                            mediaRecorder = null
                            isRecording = false
                        }
                    }

                    // Final check: Does the audio file actually exist and have content?
                    if (finalAudioPath != null) {
                        try {
                            val file = File(finalAudioPath)
                            if (!file.exists() || file.length() < 100) { 
                                finalAudioPath = null
                            }
                        } catch (e: Exception) {
                            finalAudioPath = null
                        }
                    }

                    if (logId != null) {
                        // UPDATE EXISTING
                        val updated = LifeLogEntry(
                            id = logId,
                            title = title,
                            description = description,
                            imageFilePath = capturedImageUri?.toString(),
                            audioFilePath = finalAudioPath,
                            category = category,
                            userId = userId
                        )
                        viewModel.updateEntry(updated, userId)
                    } else {
                        // SAVE NEW
                        viewModel.saveEntry(title, description, capturedImageUri?.toString(), finalAudioPath, category, userId)
                    }
                    onBackClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealGreen),
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (logId != null) "Update Entry" else "Save Entry", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}