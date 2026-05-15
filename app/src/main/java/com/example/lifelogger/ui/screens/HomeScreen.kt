package com.example.lifelogger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifelogger.ui.AuthViewModel
import com.example.lifelogger.ui.LifeLogViewModel
import com.example.lifelogger.ui.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeLoggerHomeScreen(
    viewModel: LifeLogViewModel,
    authViewModel: AuthViewModel,
    onAddClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onLogClick: (String) -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val logs by viewModel.todayLogs.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    
    // Auto-sync when user logs in or returns to home
    LaunchedEffect(currentUser) {
        viewModel.setUserId(currentUser?.id)
        val userId = currentUser?.id
        if (userId != null) {
            viewModel.syncLogs(userId)
        }
    }
    
    // Blinking animation for green dot
    val infiniteTransition = rememberInfiniteTransition(label = "blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Retrieve name from local user data
    val userName = currentUser?.fullName ?: "Achiever"
    
    var showProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LightGrayBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = TealGreen,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry", tint = Color.White)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        if (showProfileDialog) {
            ProfileDialog(
                email = currentUser?.email,
                name = userName,
                onLogout = {
                    showProfileDialog = false
                    authViewModel.logout(onLoggedOut = onNavigateToAuth)
                },
                onDismiss = { showProfileDialog = false }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                TopGreetingBar(
                    userName = userName,
                    onProfileClick = {
                        if (currentUser == null) {
                            onNavigateToAuth()
                        } else {
                            showProfileDialog = true
                        }
                    },
                    statusContent = {
                        // Network & Sync Indicators
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            if (!isOnline) {
                                // RED DOT
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Offline", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            } else if (currentUser != null) {
                                // GREEN BLINKING DOT
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Green)
                                        .alpha(alpha)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-sync", fontSize = 11.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }

            item {
                // Sync Indicator
                AnimatedVisibility(
                    visible = currentUser != null && isSyncing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = TealGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing with cloud...", fontSize = 12.sp, color = TealGreen)
                    }
                }
            }

            item {
                SummaryCard(entryCount = logs.size)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Logs", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onViewAllClick) {
                        Text("View All", color = TealGreen, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (logs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No logs for today yet. Start logging!", color = Color.Gray)
                    }
                }
            } else {
                itemsIndexed(logs) { index, log ->
                    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))

                    TimelineItem(
                        title = log.title,
                        subtitle = log.description,
                        time = timeString,
                        category = log.category,
                        isFirst = (index == 0),
                        onClick = { onLogClick(log.id) }
                    )
                }
            }
            
            // Add a smaller spacer so FAB doesn't cover last items but space is minimal
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
