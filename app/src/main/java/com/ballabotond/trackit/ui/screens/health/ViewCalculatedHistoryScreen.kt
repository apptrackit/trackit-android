package com.ballabotond.trackit.ui.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.ballabotond.trackit.data.model.HistoryEntry
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection
import com.ballabotond.trackit.utils.formatDate
import com.ballabotond.trackit.ui.components.SmoothMetricChart
import com.ballabotond.trackit.ui.components.TimeFilterButton
import com.ballabotond.trackit.ui.components.StatItem
import com.ballabotond.trackit.utils.TimeFilter

@Composable
fun ViewBMIHistoryScreen(
    navController: NavController,
    viewModel: HealthViewModel
) {
    var showModal by remember { mutableStateOf(true) }
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
                        // Header with close button
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
                                text = "BMI History",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Placeholder for symmetry
                            Spacer(modifier = Modifier.size(48.dp))
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

                    // BMI Stats Card
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
                                        text = "BMI Trend", 
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
                                            text = formattedValue, 
                                            color = Color(0xFF888888), 
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (filteredHistory.isNotEmpty()) {
                                    SmoothMetricChart(history = filteredHistory, unit = "")
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
                        Text(
                            text = "History",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
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
                                BMIHistoryItem(entry = entry)
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
fun BMIHistoryItem(entry: HistoryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Column {
                // BMI value with calculation details
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BMI value
                    Text(
                        text = String.format("%.1f", entry.value).let { 
                            if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
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
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
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
    var showModal by remember { mutableStateOf(true) }
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
                        // Header with close button
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

                            // Placeholder for symmetry
                            Spacer(modifier = Modifier.size(48.dp))
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

                    // Statistics
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
                        Text(
                            text = "History",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
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
                                HistoryItem(entry = entry, unit = unit)
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
private fun HistoryItem(entry: HistoryEntry, unit: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val formattedValue = String.format("%.1f", entry.value).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                }
                
                Text(
                    text = if (unit.isEmpty()) formattedValue else "$formattedValue $unit",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = formatDate(entry.date),
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
