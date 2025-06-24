package com.example.trackit.ui.screens.health

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
import com.example.trackit.data.model.HistoryEntry
import com.example.trackit.ui.viewmodel.HealthViewModel
import com.example.trackit.utils.formatDate
import com.example.trackit.ui.components.TimeFilterButton
import com.example.trackit.ui.screens.dashboard.SmoothMetricChart
import com.example.trackit.utils.TimeFilter

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
                    text = "Stats",
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

            // Graph - Updated to use SmoothMetricChart
            if (filteredHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF181818)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BMI Trend", 
                                color = Color.White, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold
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
                                    text = formattedValue, 
                                    color = Color.White, 
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        // Use the same chart component as dashboard
                        SmoothMetricChart(history = filteredHistory, unit = "")
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF181818), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Data",
                        color = Color(0xFF444444),
                        fontSize = 12.sp,
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
                        text = "No Data",
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

            // Statistics
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
                            StatItem(
                                label = "Min",
                                value = String.format("%.1f", stats.first).let { 
                                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                }
                            )
                            StatItem(
                                label = "Avg",
                                value = String.format("%.1f", stats.second).let { 
                                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                }
                            )
                            StatItem(
                                label = "Max",
                                value = String.format("%.1f", stats.third).let { 
                                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                }
                            )
                        }
                    }
                }
            }

            // Chart - Updated to use SmoothMetricChart
            if (filteredHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF181818)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$title Trend", 
                                color = Color.White, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold
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
                                    text = "$formattedValue${if (unit.isNotEmpty()) " $unit" else ""}", 
                                    color = Color.White, 
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        // Use the same chart component as dashboard
                        SmoothMetricChart(history = filteredHistory, unit = unit)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF181818), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Data",
                        color = Color(0xFF444444),
                        fontSize = 12.sp,
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
                        text = "No Data",
                        color = Color(0xFF444444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            } else {
                LazyColumn {
                    items(filteredHistory) { entry ->
                        HistoryItem(entry = entry, unit = unit)
                        Divider(color = Color(0xFF333333), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: HistoryEntry, unit: String) {
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
        
        val formattedValue = String.format("%.1f", entry.value).let { 
            if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
        }
        
        Text(
            text = if (unit.isEmpty()) formattedValue else "$formattedValue $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
