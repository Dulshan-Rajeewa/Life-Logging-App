package com.example.lifelogger.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ALL OUR COLORS DEFINED HERE
val TealGreen = Color(0xFF1CB096)
val LightGrayBg = Color(0xFFF5F7FA)
val TextDark = Color(0xFF2D3142)
val TextLight = Color(0xFF9CA3AF)

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Study" -> Color(0xFF3B82F6) // Blue
        "Workout" -> Color(0xFFEF4444) // Red
        "Events" -> Color(0xFFF59E0B) // Amber
        "Reflections" -> Color(0xFF8B5CF6) // Purple
        "Travel" -> Color(0xFF10B981) // Emerald
        "Work" -> Color(0xFF6B7280) // Gray
        else -> TealGreen // General/Default
    }
}

@Composable
fun TopGreetingBar(
    modifier: Modifier = Modifier,
    userName: String = "Student",
    statusContent: @Composable (() -> Unit)? = null,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date()), color = TextLight, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Good day, $userName!",
                    color = TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (statusContent != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    statusContent()
                }
            }
        }
        Surface(
            onClick = onProfileClick,
            shape = RoundedCornerShape(12.dp),
            color = TextDark,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * A simple dialog to show account details and logout button
 */
@Composable
fun ProfileDialog(
    email: String?,
    name: String?,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Details", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Name: ${name ?: "N/A"}", fontWeight = FontWeight.Medium)
                Text("Email: ${email ?: "N/A"}", color = TextLight)
            }
        },
        confirmButton = {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Logout", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextDark)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun SummaryCard(entryCount: Int = 0) {
    // We'll define a daily goal of 5 entries to calculate the percentage
    val dailyGoal = 5
    val targetProgress = (entryCount.toFloat() / dailyGoal).coerceAtMost(1f)
    
    // Animate the progress change
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    val percentage = (animatedProgress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp), // Slightly more rounded as per image
        colors = CardDefaults.cardColors(containerColor = TealGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Log", 
                    color = Color.White, 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "For Today", 
                    color = Color.White, 
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$entryCount ${if (entryCount == 1) "Entry" else "Entries"} logged", 
                    color = Color.White.copy(alpha = 0.9f), 
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(contentAlignment = Alignment.Center) {
                // Background circle for the progress indicator
                CircularProgressIndicator(
                    progress = 1f,
                    color = Color.White.copy(alpha = 0.2f),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(70.dp)
                )
                // The actual progress indicator
                CircularProgressIndicator(
                    progress = animatedProgress,
                    color = Color.White,
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(70.dp)
                )
                Text(
                    text = "$percentage%", 
                    color = Color.White, 
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    title: String, 
    subtitle: String, 
    time: String, 
    category: String = "General", 
    isFirst: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(30.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (isFirst) TealGreen else Color.Transparent, CircleShape)
                    .border(2.dp, TealGreen, CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.LightGray))
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, color = if (isFirst) TealGreen else TextDark, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                val categoryColor = getCategoryColor(category)
                Surface(
                    color = categoryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = category,
                        color = categoryColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(text = subtitle, color = TextLight, fontSize = 12.sp, maxLines = 1)
        }

        Text(text = time, color = TextDark, fontSize = 14.sp)
    }
}
