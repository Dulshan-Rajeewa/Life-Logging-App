package com.example.lifelogger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.media.MediaPlayer
import coil.compose.AsyncImage
import com.example.lifelogger.data.LifeLogEntry
import com.example.lifelogger.ui.LifeLogViewModel
import com.example.lifelogger.ui.components.LightGrayBg
import com.example.lifelogger.ui.components.TealGreen
import com.example.lifelogger.ui.components.TextDark
import com.example.lifelogger.ui.components.TextLight
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    logId: String,
    viewModel: LifeLogViewModel,
    userId: String?,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val logEntry = remember { mutableStateOf<LifeLogEntry?>(null) }
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(logId) {
        logEntry.value = viewModel.getLogById(logId)
    }

    val log = logEntry.value

    Scaffold(
        containerColor = LightGrayBg,
        topBar = {
            TopAppBar(
                title = { Text("Log Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (log != null) {
                        IconButton(onClick = { onEditClick(log.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TealGreen)
                        }
                        IconButton(onClick = {
                            viewModel.deleteEntry(log.id, userId)
                            onDeleteSuccess()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (log == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Title and Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = log.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            lineHeight = 34.sp
                        )
                        Text(
                            text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                            color = TextLight,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    val categoryColor = com.example.lifelogger.ui.components.getCategoryColor(log.category)
                    Surface(
                        color = categoryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = log.category,
                            color = categoryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Image if available
                if (!log.imageFilePath.isNullOrEmpty()) {
                    OutlinedCard(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = log.imageFilePath,
                            contentDescription = "Log Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Description Section
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TealGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = log.description,
                            fontSize = 16.sp,
                            color = TextDark.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Audio if available
                if (!log.audioFilePath.isNullOrEmpty()) {
                    val audioFileExists = remember(log.audioFilePath) {
                        if (log.audioFilePath.startsWith("http")) true
                        else try { File(log.audioFilePath).exists() } catch (e: Exception) { false }
                    }

                    if (audioFileExists) {
                        Surface(
                            color = TealGreen.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealGreen.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Voice Note",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        try {
                                            if (mediaPlayer?.isPlaying == true) {
                                                mediaPlayer?.stop()
                                                mediaPlayer?.release()
                                                mediaPlayer = null
                                            } else {
                                                mediaPlayer?.release()
                                                mediaPlayer = MediaPlayer().apply {
                                                    if (log.audioFilePath.startsWith("content://")) {
                                                        setDataSource(context, Uri.parse(log.audioFilePath))
                                                    } else {
                                                        setDataSource(log.audioFilePath)
                                                    }

                                                    if (log.audioFilePath.startsWith("http")) {
                                                        prepareAsync()
                                                        setOnPreparedListener { start() }
                                                    } else {
                                                        prepare()
                                                        start()
                                                    }
                                                    setOnCompletionListener {
                                                        it.release()
                                                        mediaPlayer = null
                                                    }
                                                    setOnErrorListener { mp, _, _ ->
                                                        mp.release()
                                                        mediaPlayer = null
                                                        true
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("LogDetailScreen", "Audio playback failed", e)
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealGreen),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (mediaPlayer?.isPlaying == true) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null, 
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            if (mediaPlayer?.isPlaying == true) "Stop Recording" else "Play Recording", 
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Show a placeholder or message if the file is missing
                        Text(
                            text = "Audio attachment not found.",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}