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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.ui.components.DatePickerDialog
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.formatDate
import com.example.lifetracker.ui.screens.health.ProgressScreen
import com.example.lifetracker.utils.TimeFilter
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
                                text = "Stats",
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
    
    // Add a padding to the range to avoid values touching the edges
    val range = (maxValue - minValue).coerceAtLeast(0.1f)
    val paddedMin = (minValue - range * 0.1f).coerceAtLeast(0f)
    val paddedMax = maxValue + range * 0.1f
    val valueRange = paddedMax - paddedMin

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp) // Increased height to accommodate labels
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Chart area height
                .padding(start = 50.dp, end = 8.dp, top = 8.dp, bottom = 8.dp) // Increased left padding from 40dp to 50dp
        ) {
            val width = size.width
            val height = size.height
            val chartWidth = width
            val chartHeight = height - 20.dp.toPx() // Leave space for x-axis labels
            
            // Draw horizontal grid lines and y-axis labels
            val ySteps = 5
            for (i in 0..ySteps) {
                val y = chartHeight - (i.toFloat() / ySteps.toFloat() * chartHeight)
                
                // Draw horizontal grid line
                drawLine(
                    color = Color(0xFF333333),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Calculate the value for this grid line
                val value = paddedMin + (i.toFloat() / ySteps.toFloat() * valueRange)
                val formattedValue = String.format("%.1f", value).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                }
                
                // Draw y-axis label
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        formattedValue,
                        -45.dp.toPx(), // Increased from -35dp to -45dp to move labels further from the edge
                        y + 5.dp.toPx(), // Align with grid line, adjust for text height
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }
            }
            
            // Draw smooth curve through data points
            if (sortedHistory.size > 1) {
                val path = Path()
                val points = sortedHistory.mapIndexed { index, entry ->
                    val x = index * chartWidth / (sortedHistory.size - 1)
                    val normalizedValue = (entry.value - paddedMin) / valueRange
                    val y = chartHeight - normalizedValue * chartHeight
                    Offset(x, y)
                }
                
                // Start the path at the first point
                path.moveTo(points[0].x, points[0].y)
                
                // Draw smooth curve through points
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val current = points[i]
                    
                    // Simple cubic interpolation for smoothing
                    // You can adjust the control points for different smoothness
                    val controlX1 = prev.x + (current.x - prev.x) / 3
                    val controlY1 = prev.y
                    val controlX2 = current.x - (current.x - prev.x) / 3
                    val controlY2 = current.y
                    
                    path.cubicTo(
                        controlX1, controlY1,
                        controlX2, controlY2,
                        current.x, current.y
                    )
                }
                
                // Draw the smooth path
                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.5.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            } else if (sortedHistory.size == 1) {
                // For single point, draw a horizontal line
                val entry = sortedHistory[0]
                val normalizedValue = (entry.value - paddedMin) / valueRange
                val y = chartHeight - normalizedValue * chartHeight
                
                drawLine(
                    color = Color(0xFF2196F3),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        // Draw date labels below the chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(top = 210.dp, start = 50.dp, end = 8.dp), // Increased left padding from 40dp to 50dp
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            if (sortedHistory.size == 1) {
                // Center the single date
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = dateFormat.format(Date(sortedHistory[0].date)),
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            } else if (sortedHistory.size <= 7) {
                // Show all dates if there are 7 or fewer
                sortedHistory.forEach { entry ->
                    Text(
                        text = dateFormat.format(Date(entry.date)),
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            } else {
                // Show only a subset of dates for readability
                val dates = if (sortedHistory.size > 10) {
                    // For many points, show first, some intermediate, and last
                    val step = sortedHistory.size / 4
                    listOf(
                        sortedHistory.first(),
                        sortedHistory[step],
                        sortedHistory[step * 2],
                        sortedHistory[step * 3],
                        sortedHistory.last()
                    )
                } else {
                    // For medium dataset, show every other point
                    sortedHistory.filterIndexed { index, _ -> index % 2 == 0 || index == sortedHistory.lastIndex }
                }
                
                // Use a weighted arrangement for the date labels
                Box(modifier = Modifier.fillMaxWidth()) {
                    dates.forEachIndexed { index, entry ->
                        val position = if (dates.size > 1) {
                            index.toFloat() / (dates.size - 1)
                        } else 0.5f
                        
                        Text(
                            text = dateFormat.format(Date(entry.date)),
                            color = Color.Gray,
                            fontSize = 10.sp,
                            modifier = Modifier.align(
                                when {
                                    position < 0.2f -> Alignment.CenterStart
                                    position > 0.8f -> Alignment.CenterEnd
                                    position < 0.4f -> Alignment.Start
                                    position > 0.6f -> Alignment.End
                                    else -> Alignment.Center
                                } as Alignment
                            )
                        )
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
