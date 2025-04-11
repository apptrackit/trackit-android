package com.example.lifetracker.ui.screens.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.ui.components.DatePickerDialog
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.TimeFilter
import com.example.lifetracker.utils.formatDate
import com.example.lifetracker.ui.screens.health.ProgressScreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditMetricScreen(
    title: String,
    metricName: String,
    unit: String,
    navController: NavController,
    viewModel: HealthViewModel,
    onSave: (String, Long) -> Unit
) {
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                // Header with back button, title, and plus button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button and title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Plus button with improved styling
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A1A))
                            .clickable {
                                navController.navigate("add_metric_data/$metricName/$unit/$title")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Entry",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Time filter buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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

                if (filteredHistory.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "BMI Statistics",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
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

                // Graph
                MetricHistoryChart(history = filteredHistory, unit = unit)

                // History section header with Edit/Done button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Edit/Done button
                    Text(
                        text = if (isEditMode) "Done" else "Edit",
                        color = Color.Blue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { isEditMode = !isEditMode }
                    )
                }
            }

            items(filteredHistory) { entry ->
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
                Divider(color = Color(0xFF333333), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun TimeFilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color(0xFF333333),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(90.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Composable
fun MetricHistoryChart(history: List<HistoryEntry>, unit: String) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Data",
                color = Color(0xFF444444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        return
    }

    val sortedHistory = history.sortedBy { it.date }
    val minValue = sortedHistory.minOf { it.value }
    val maxValue = sortedHistory.maxOf { it.value }
    // Add a small buffer to the range to avoid division by zero and to make the graph look better
    val valueRange = if (maxValue > minValue) maxValue - minValue + 0.1f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // Increased height to accommodate labels below baseline
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Chart area height
        ) {
            val width = size.width
            val height = size.height
            val xStep = if (sortedHistory.size > 1) width / (sortedHistory.size - 1) else width

            // Draw axis
            drawLine(Color.Gray, Offset(0f, height), Offset(width, height))
            drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, height))

            // Draw data points and lines
            val path = Path()
            sortedHistory.forEachIndexed { index, entry ->
                val x = if (sortedHistory.size > 1) index * xStep else width / 2
                val normalizedValue = (entry.value - minValue) / valueRange
                val y = height - normalizedValue * height * 0.9f // Leave some margin at the top

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                drawCircle(Color(0xFF2196F3), 4.dp.toPx(), Offset(x, y))
            }

            drawPath(path, Color(0xFF2196F3), style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
        }

        // Draw date labels below the chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(top = 210.dp), // Position below the chart
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            if (sortedHistory.size == 1) {
                // Center the single date
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = dateFormat.format(Date(sortedHistory[0].date)),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                // Distribute dates evenly
                sortedHistory.forEach { entry ->
                    Text(
                        text = dateFormat.format(Date(entry.date)),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete button (only visible in edit mode)
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Red, CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Value with unit
        Text(
            text = "${String.format("%.1f", entry.value).let { 
                if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
            }} $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Date
        Text(
            text = formatDate(entry.date),
            color = Color.Gray,
            fontSize = 14.sp
        )

        // Arrow (only visible when in edit mode)
        if (isEditMode) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Edit Entry",
                tint = Color.Gray,
                modifier = Modifier.clickable { onEdit(entry) }
            )
        }
    }
}
