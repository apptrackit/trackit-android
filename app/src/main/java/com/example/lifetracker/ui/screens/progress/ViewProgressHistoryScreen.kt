package com.example.lifetracker.ui.screens.progress

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
import com.example.lifetracker.ui.components.MetricHistoryChart
import com.example.lifetracker.ui.components.StatItem
import com.example.lifetracker.ui.components.MetricHistoryItem
import com.example.lifetracker.ui.components.TimeFilterButton

@Composable
fun ViewProgressHistoryScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    metricName: String,
    unit: String
) {
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.MONTH) }

    // Get metric history
    val metricHistory = viewModel.getMetricHistory(metricName, unit)

    // Filter history based on selected time period
    val filteredHistory = when (selectedTimeFilter) {
        TimeFilter.WEEK -> {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            metricHistory.filter { it.date >= weekAgo }
        }
        TimeFilter.MONTH -> {
            val monthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            metricHistory.filter { it.date >= monthAgo }
        }
        TimeFilter.YEAR -> {
            val yearAgo = System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L
            metricHistory.filter { it.date >= yearAgo }
        }
    }

    // Calculate metric statistics
    val metricStats = if (filteredHistory.isNotEmpty()) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance the header
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Min",
                    value = "${String.format("%.1f", metricStats.first)} $unit"
                )
                StatItem(
                    label = "Avg",
                    value = "${String.format("%.1f", metricStats.second)} $unit"
                )
                StatItem(
                    label = "Max",
                    value = "${String.format("%.1f", metricStats.third)} $unit"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            if (filteredHistory.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    MetricHistoryChart(
                        history = filteredHistory,
                        unit = unit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History list
            LazyColumn {
                items(filteredHistory) { entry ->
                    MetricHistoryItem(
                        entry = entry,
                        unit = unit
                    )
                }
            }
        }
    }
}
