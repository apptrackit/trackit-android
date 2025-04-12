package com.example.lifetracker.ui.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import calculateBodySurfaceArea
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.TimeFilter
import com.example.lifetracker.utils.formatDate
import com.example.lifetracker.ui.components.MetricHistoryChart
import com.example.lifetracker.ui.components.StatItem
import com.example.lifetracker.ui.components.MetricHistoryItem
import com.example.lifetracker.ui.components.TimeFilterButton
import com.example.lifetracker.utils.calculateBMI
import com.example.lifetracker.utils.calculateFFMI
import com.example.lifetracker.utils.calculateLeanBodyMass
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ViewProgressHistoryScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    metricName: String,
    unit: String
) {
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.MONTH) }
    val isCalculatedMetric = metricName in listOf("BMI", "Lean Body Mass", "Body Surface Area", "FFMI")

    // Get base metrics history for calculations
    val weightHistory by remember { mutableStateOf(viewModel.getMetricHistory("Weight", "kg")) }
    val heightHistory by remember { mutableStateOf(viewModel.getMetricHistory("Height", "cm")) }
    val bodyFatHistory by remember { mutableStateOf(viewModel.getMetricHistory("Body Fat", "%")) }

    // Calculate the metric history on the fly
    val allHistory = remember(weightHistory, heightHistory, bodyFatHistory) {
        when (metricName) {
            "BMI" -> {
                // Get all unique dates from both weight and height history
                val allDates = (weightHistory.map { it.date } + heightHistory.map { it.date }).distinct().sorted()
                
                allDates.mapNotNull { date ->
                    val weight = weightHistory.find { it.date <= date }?.value
                    val height = heightHistory.find { it.date <= date }?.value
                    
                    if (weight != null && height != null) {
                        HistoryEntry(
                            value = calculateBMI(weight, height),
                            unit = "",
                            date = date,
                            metricName = "BMI"
                        )
                    } else null
                }
            }
            "Lean Body Mass" -> {
                // Get all unique dates from both weight and body fat history
                val allDates = (weightHistory.map { it.date } + bodyFatHistory.map { it.date }).distinct().sorted()
                
                allDates.mapNotNull { date ->
                    val weight = weightHistory.find { it.date <= date }?.value
                    val bodyFat = bodyFatHistory.find { it.date <= date }?.value
                    
                    if (weight != null && bodyFat != null) {
                        HistoryEntry(
                            value = calculateLeanBodyMass(weight, bodyFat),
                            unit = "kg",
                            date = date,
                            metricName = "Lean Body Mass"
                        )
                    } else null
                }
            }
            "Body Surface Area" -> {
                // Get all unique dates from both weight and height history
                val allDates = (weightHistory.map { it.date } + heightHistory.map { it.date }).distinct().sorted()
                
                allDates.mapNotNull { date ->
                    val weight = weightHistory.find { it.date <= date }?.value
                    val height = heightHistory.find { it.date <= date }?.value
                    
                    if (weight != null && height != null) {
                        HistoryEntry(
                            value = calculateBodySurfaceArea(weight, height),
                            unit = "m²",
                            date = date,
                            metricName = "Body Surface Area"
                        )
                    } else null
                }
            }
            "FFMI" -> {
                // Get all unique dates from weight, height, and body fat history
                val allDates = (weightHistory.map { it.date } + heightHistory.map { it.date } + bodyFatHistory.map { it.date }).distinct().sorted()
                
                allDates.mapNotNull { date ->
                    val weight = weightHistory.find { it.date <= date }?.value
                    val height = heightHistory.find { it.date <= date }?.value
                    val bodyFat = bodyFatHistory.find { it.date <= date }?.value
                    
                    if (weight != null && height != null && bodyFat != null) {
                        HistoryEntry(
                            value = calculateFFMI(weight, height, bodyFat),
                            unit = "",
                            date = date,
                            metricName = "FFMI"
                        )
                    } else null
                }
            }
            else -> viewModel.getMetricHistory(metricName, unit)
        }
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
                // Header with back button and title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 8.dp),
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
                        text = metricName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Time filter buttons centered
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeFilterButton(
                        text = "Week",
                        isSelected = selectedTimeFilter == TimeFilter.WEEK,
                        onClick = { selectedTimeFilter = TimeFilter.WEEK }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TimeFilterButton(
                        text = "Month",
                        isSelected = selectedTimeFilter == TimeFilter.MONTH,
                        onClick = { selectedTimeFilter = TimeFilter.MONTH }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TimeFilterButton(
                        text = "Year",
                        isSelected = selectedTimeFilter == TimeFilter.YEAR,
                        onClick = { selectedTimeFilter = TimeFilter.YEAR }
                    )
                }

                // Statistics card
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
                                text = "$metricName Statistics",
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
                                    } + " $unit"
                                )
                                StatItem(
                                    label = "Avg",
                                    value = String.format("%.1f", entryStats.second).let {
                                        if (it.endsWith(".0")) it.substring(0, it.length - 2) else it
                                    } + " $unit"
                                )
                                StatItem(
                                    label = "Max",
                                    value = String.format("%.1f", entryStats.third).let {
                                        if (it.endsWith(".0")) it.substring(0, it.length - 2) else it
                                    } + " $unit"
                                )
                            }
                        }
                    }
                }

                // Chart
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
                        val sortedHistory = filteredHistory.sortedBy { it.date }
                        val xStep = if (sortedHistory.size > 1) width / (sortedHistory.size - 1) else width

                        // Draw axis
                        drawLine(Color.Gray, Offset(0f, height), Offset(width, height))
                        drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, height))

                        // Draw data points and lines
                        val path = Path()
                        sortedHistory.forEachIndexed { index, entry ->
                            val x = if (sortedHistory.size > 1) index * xStep else width / 2
                            val normalizedValue = (entry.value - entryStats.first) / (entryStats.third - entryStats.first)
                            val y = height - normalizedValue * height * 0.9f // Leave some margin at the top

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }

                            drawCircle(Color(0xFF2196F3), 4.dp.toPx(), Offset(x, y))
                        }

                        drawPath(path, Color(0xFF2196F3), style = Stroke(3.dp.toPx()))
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
                        val sortedHistory = filteredHistory.sortedBy { it.date }

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

                // History section header
                Text(
                    text = "History",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            items(filteredHistory) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                }
                Divider(color = Color(0xFF333333), thickness = 1.dp)
            }
        }
    }
}
