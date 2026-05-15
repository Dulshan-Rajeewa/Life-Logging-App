package com.example.lifelogger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifelogger.ui.LifeLogViewModel
import com.example.lifelogger.ui.components.LightGrayBg
import com.example.lifelogger.ui.components.TealGreen
import com.example.lifelogger.ui.components.TextDark
import com.example.lifelogger.ui.components.TimelineItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllLogsScreen(
    viewModel: LifeLogViewModel,
    onBackClick: () -> Unit,
    onLogClick: (String) -> Unit
) {
    val logs by viewModel.filteredLogs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedDate by viewModel.selectedDateFilter.collectAsState()
    
    val categories = listOf("All", "General", "Study", "Workout", "Events", "Reflections", "Travel", "Work")

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDateFilter(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK", color = TealGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextDark)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = LightGrayBg,
        topBar = {
            TopAppBar(
                title = { Text("All My Logs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarMonth, 
                            contentDescription = "Filter by date",
                            tint = if (selectedDate != null) TealGreen else TextDark
                        )
                    }
                    if (selectedDate != null) {
                        IconButton(onClick = { viewModel.updateDateFilter(null) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear date filter", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                if (selectedDate != null) {
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate!!))
                    Surface(
                        color = TealGreen.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Showing logs for: $dateStr",
                                color = TealGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.Clear, 
                                contentDescription = "Clear", 
                                tint = TealGreen,
                                modifier = Modifier.size(18.dp).clickable { viewModel.updateDateFilter(null) }
                            )
                        }
                    }
                }
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search logs...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealGreen,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Category Filter
                Text("Filter by Category", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.updateCategoryFilter(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (logs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                        Text("No logs found matching your criteria.", color = Color.Gray)
                    }
                }
            } else {
                itemsIndexed(logs) { index, log ->
                    val timeString = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(log.timestamp))

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
            
            // Padding at the bottom
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
