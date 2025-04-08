package com.example.lifetracker.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.ui.components.AddMetricButton
import com.example.lifetracker.ui.components.AddMetricPopup
import com.example.lifetracker.ui.components.ClickableMetricCardWithChart
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

@SuppressLint("DefaultLocale")
@Composable
fun ProgressScreen(
    onNavigateToEditMetric: (String) -> Unit,
    onNavigateToViewBMIHistory: () -> Unit,
    viewModel: HealthViewModel,
    navController: NavController
) {
    // State for showing the popup
    var showAddMetricPopup by remember { mutableStateOf(false) }

    // Get latest values from history
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Get history data for charts
    val weightHistory = viewModel.getMetricHistory("Weight", "kg")
    val heightHistory = viewModel.getMetricHistory("Height", "cm")
    val bodyFatHistory = viewModel.getMetricHistory("Body Fat", "%")
    val bmiHistory = viewModel.getMetricHistory("BMI", "")

    // Format values based on history
    val formattedWeight = if (weightHistory.isEmpty()) "No Data" else {
        val value = latestWeight ?: 0f
        if (value > 0) {
            val formatted = String.format("%.1f", value)
            if (formatted.endsWith(".0")) formatted.substring(
                0,
                formatted.length - 2
            ) else formatted
        } else "No Data"
    }

    val formattedHeight = if (heightHistory.isEmpty()) "No Data" else {
        val value = latestHeight ?: 0f
        if (value > 0) value.toInt().toString() else "No Data"
    }

    val formattedBodyFat = if (bodyFatHistory.isEmpty()) "No Data" else {
        val value = latestBodyFat ?: 0f
        if (value > 0) {
            val formatted = String.format("%.1f", value)
            if (formatted.endsWith(".0")) formatted.substring(
                0,
                formatted.length - 2
            ) else formatted
        } else "No Data"
    }

    // Calculate BMI with null safety
    val bmi = if (weightHistory.isNotEmpty() && heightHistory.isNotEmpty() &&
        latestWeight != null && latestHeight != null &&
        latestWeight > 0 && latestHeight > 0
    ) {
        calculateBMI(latestWeight, latestHeight)
    } else {
        0f
    }

    val formattedBmi = if (bmi > 0) {
        val formatted = String.format("%.1f", bmi)
        if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
    } else "No Data"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with title and add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Metric cards grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),  // reduced height
                    color = Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(12.dp)  // rounded corners
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricCardNoChart(
                            title = "Weight",
                            value = formattedWeight,
                            unit = "kg",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Edit Weight",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null  // removes ripple effect
                                ) { onNavigateToEditMetric("Weight") },
                            tint = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),  // reduced height
                    color = Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(12.dp)  // rounded corners
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricCardNoChart(
                            title = "Height",
                            value = formattedHeight,
                            unit = "cm",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Edit Height",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null  // removes ripple effect
                                ) { onNavigateToEditMetric("Height") },
                            tint = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),  // reduced height
                    color = Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(12.dp)  // rounded corners
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricCardNoChart(
                            title = "BMI",
                            value = formattedBmi,
                            unit = "",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View BMI History",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null  // removes ripple effect
                                ) { onNavigateToViewBMIHistory() },
                            tint = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),  // reduced height
                    color = Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(12.dp)  // rounded corners
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricCardNoChart(
                            title = "Body Fat",
                            value = formattedBodyFat,
                            unit = "%",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Edit Body Fat",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null  // removes ripple effect
                                ) { onNavigateToEditMetric("Body Fat") },
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable 
private fun MetricCardNoChart(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 16.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }
}