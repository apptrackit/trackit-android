package com.ballabotond.trackit.ui.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.ballabotond.trackit.data.model.HistoryEntry
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection
import com.ballabotond.trackit.ui.components.SmoothMetricChart
import com.ballabotond.trackit.ui.components.TimeFilterButton
import com.ballabotond.trackit.ui.components.StatItem
import com.ballabotond.trackit.utils.formatDate
import com.ballabotond.trackit.utils.TimeFilter

@Composable
fun EditMetricScreen(
    title: String,
    metricName: String,
    unit: String,
    navController: NavController,
    viewModel: HealthViewModel,
    onSave: (String, Long) -> Unit
) {
    var showModal by remember { mutableStateOf(true) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.MONTH) }

    // Use a key to force recomposition when data changes
    var refreshKey by remember { mutableStateOf(0) }

    // Get history data with the refresh key as a dependency
    val allHistory by remember(refreshKey, metricName, unit) {
        mutableStateOf(viewModel.getMetricHistory(metricName, unit))
    }

    // Filter history based on selected time period
    val filteredHistory = when (selectedTimeFilter) {
        TimeFilter.WEEK -> {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            allHistory.filter { it.date >= weekAgo }
        }
        TimeFilter.MONTH -> {
            val monthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            allHistory.filter { it.date >= monthAgo }
        }
        TimeFilter.YEAR -> {
            val yearAgo = System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L
            allHistory.filter { it.date >= yearAgo }
        }
    }
    
    val entryStats = if (filteredHistory.isNotEmpty()) {
        val values = filteredHistory.map { it.value }
        val avg = values.average()
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        Triple(min, avg, max)
    } else {
        Triple(0f, 0.0, 0f)
    }

    if (showModal) {
        Dialog(onDismissRequest = { 
            showModal = false
            navController.popBackStack()
        }) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF1A1A1A)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp)
                ) {
                    item {
                        // Header with close button and title
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    showModal = false
                                    navController.popBackStack()
                                }
                            ) {
                                Icon(
                                    imageVector = FeatherIconsCollection.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF007AFF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Plus button
                            IconButton(
                                onClick = {
                                    navController.navigate("add_metric_data/$metricName/$unit/$title")
                                }
                            ) {
                                Icon(
                                    imageVector = FeatherIconsCollection.Add,
                                    contentDescription = "Add Entry",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    item {
                        // Time filter buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TimeFilterButton(
                                text = "Week",
                                isSelected = selectedTimeFilter == TimeFilter.WEEK,
                                onClick = { selectedTimeFilter = TimeFilter.WEEK }
                            )

                            TimeFilterButton(
                                text = "Month",
                                isSelected = selectedTimeFilter == TimeFilter.MONTH,
                                onClick = { selectedTimeFilter = TimeFilter.MONTH }
                            )

                            TimeFilterButton(
                                text = "Year",
                                isSelected = selectedTimeFilter == TimeFilter.YEAR,
                                onClick = { selectedTimeFilter = TimeFilter.YEAR }
                            )
                        }
                    }

                    // Stats card
                    if (filteredHistory.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2A2A2A)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = "Statistics",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        StatItem(
                                            label = "Min",
                                            value = String.format("%.1f", entryStats.first).let {
                                                if (it.endsWith(".0")) it.substring(0, it.length - 2) else it
                                            }
                                        )
                                        StatItem(
                                            label = "Avg",
                                            value = String.format("%.1f", entryStats.second).let {
                                                if (it.endsWith(".0")) it.substring(0, it.length - 2) else it
                                            }
                                        )
                                        StatItem(
                                            label = "Max",
                                            value = String.format("%.1f", entryStats.third).let {
                                                if (it.endsWith(".0")) it.substring(0, it.length - 2) else it
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chart card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A2A)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$title Trend", 
                                        color = Color.White, 
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    // Display the latest value if available
                                    if (filteredHistory.isNotEmpty()) {
                                        val latestValue = filteredHistory.maxByOrNull { it.date }?.value
                                        val formattedValue = latestValue?.let {
                                            String.format("%.1f", it).let { 
                                                if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                            }
                                        } ?: ""
                                        
                                        Text(
                                            text = if (unit.isEmpty()) formattedValue else "$formattedValue $unit", 
                                            color = Color(0xFF888888), 
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (filteredHistory.isNotEmpty()) {
                                    SmoothMetricChart(history = filteredHistory, unit = unit)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No Data",
                                            color = Color(0xFF555555),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // History section header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "History",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Edit/Done button
                            Text(
                                text = if (isEditMode) "Done" else "Edit",
                                color = Color(0xFF007AFF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { isEditMode = !isEditMode }
                            )
                        }
                    }

                    // History items
                    if (filteredHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Data",
                                    color = Color(0xFF555555),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(filteredHistory) { entry ->
                            Box(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                HistoryItem(
                                    entry = entry,
                                    unit = unit,
                                    isEditMode = isEditMode,
                                    onDelete = {
                                        viewModel.deleteHistoryEntry(metricName, entry)
                                        refreshKey++
                                    },
                                    onEdit = { historyEntry ->
                                        navController.navigate(
                                            "edit_metric_data/$metricName/$unit/$title/${historyEntry.value}/${historyEntry.date}"
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Bottom padding for safe area
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    entry: HistoryEntry,
    unit: String,
    isEditMode: Boolean,
    onDelete: () -> Unit,
    onEdit: (HistoryEntry) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isEditMode) Modifier.clickable { onEdit(entry) } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button (only visible in edit mode)
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFFF3B30), CircleShape)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FeatherIconsCollection.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    // Value with unit
                    Text(
                        text = "${String.format("%.1f", entry.value).let { 
                            if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                        }}${if (unit.isNotEmpty()) " $unit" else ""}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Date
                    Text(
                        text = formatDate(entry.date),
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Arrow (only visible when in edit mode)
            if (isEditMode) {
                Icon(
                    imageVector = FeatherIconsCollection.ChevronRight,
                    contentDescription = "Edit Entry",
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
