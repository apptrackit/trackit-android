package com.example.lifetracker.ui.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.TimeFilter
import com.example.lifetracker.utils.formatDate
import com.example.lifetracker.ui.components.TimeFilterButton
import com.example.lifetracker.ui.components.MetricHistoryChart

@Composable
fun ViewBMIHistoryScreen(
    navController: NavController,
    viewModel: HealthViewModel
) {
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.MONTH) }

    // Get BMI history
    val bmiHistory = viewModel.getMetricHistory("BMI", "")

    // Filter history based on selected time period
    val filteredHistory = when (selectedTimeFilter) {
        TimeFilter.WEEK -> {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            bmiHistory.filter { it.date >= weekAgo }
        }
        TimeFilter.MONTH -> {
            val monthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            bmiHistory.filter { it.date >= monthAgo }
        }
        TimeFilter.YEAR -> {
            val yearAgo = System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L
            bmiHistory.filter { it.date >= yearAgo }
        }
    }

    // Calculate BMI statistics
    val bmiStats = if (filteredHistory.isNotEmpty()) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
                    text = "BMI History",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
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
            // BMI Stats Card
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
                                value = String.format("%.1f", bmiStats.first).let { 
                                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                }
                            )
                            StatItem(
                                label = "Avg",
                                value = String.format("%.1f", bmiStats.second).let { 
                                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                }
                            )
                            StatItem(
                                label = "Max",
                                value = String.format("%.1f", bmiStats.third).let { 
                                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                }
                            )
                        }
                    }
                }
            }



            // Graph
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
                            text = "BMI Trend",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        MetricHistoryChart(history = filteredHistory, unit = "")
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No BMI data available",
                        color = Color(0xFF444444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // History section header
            Text(
                text = "History",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            // History items
            if (filteredHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history entries for this period",
                        color = Color(0xFF444444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            } else {
                LazyColumn {
                    items(filteredHistory) { entry ->
                        BMIHistoryItem(entry = entry)
                        Divider(color = Color(0xFF333333), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BMIHistoryItem(entry: HistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BMI value with calculation details
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BMI value
            Text(
                text = String.format("%.1f", entry.value).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Calculation details (weight and height)
            if (entry.weight != null && entry.height != null) {
                Text(
                    text = " (${String.format("%.1f", entry.weight).let { 
                        if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                    }} kg, ${entry.height.toInt()} cm)",
                    color = Color(0xFF888888),
                    fontSize = 10.sp
                )
            }
        }

        // Date
        Text(
            text = formatDate(entry.date),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewCalculatedHistoryScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    metricName: String,
    unit: String,
    title: String
) {
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.MONTH) }

    // Get history data
    val history = viewModel.getMetricHistory(metricName, unit)

    // Filter history based on selected time period
    val filteredHistory = when (selectedTimeFilter) {
        TimeFilter.WEEK -> {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            history.filter { it.date >= weekAgo }
        }
        TimeFilter.MONTH -> {
            val monthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            history.filter { it.date >= monthAgo }
        }
        TimeFilter.YEAR -> {
            val yearAgo = System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L
            history.filter { it.date >= yearAgo }
        }
    }

    // Calculate statistics
    val stats = if (filteredHistory.isNotEmpty()) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time filter buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics
            if (filteredHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Statistics",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Min", String.format("%.1f", stats.first))
                            StatItem("Avg", String.format("%.1f", stats.second))
                            StatItem("Max", String.format("%.1f", stats.third))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            MetricHistoryChart(
                history = filteredHistory,
                unit = unit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // History
            Text(
                text = "History",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(filteredHistory) { entry ->
                    HistoryItem(entry = entry)
                    Divider(color = Color(0xFF333333))
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: HistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDate(entry.date),
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = String.format("%.1f", entry.value),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}