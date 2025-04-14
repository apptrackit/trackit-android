package com.example.lifetracker.ui.screens.health

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
import com.example.lifetracker.data.model.HealthMetrics
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.lifetracker.utils.TimeFilter
import com.example.lifetracker.ui.screens.health.AddMetricDataScreen
import com.example.lifetracker.ui.screens.health.EditMetricDataScreen
import com.example.lifetracker.ui.screens.health.EditMetricScreen

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
            // Header with title
            Text(
                text = "Progress",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Replace Column with LazyColumn for scrolling
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // BODY MEASUREMENTS Section
                item {
                    Text(
                        text = "BODY MEASUREMENTS",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A1A1A),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Weight measurement
                            MetricRowWithArrow(
                                title = "Weight",
                                value = formattedWeight,
                                unit = "kg",
                                onClick = { onNavigateToEditMetric("Weight") }
                            )
                            
                            // Height measurement
                            MetricRowWithArrow(
                                title = "Height",
                                value = formattedHeight,
                                unit = "cm",
                                onClick = { onNavigateToEditMetric("Height") }
                            )
                            
                            // Body Fat measurement
                            MetricRowWithArrow(
                                title = "Body Fat",
                                value = formattedBodyFat,
                                unit = "%",
                                onClick = { onNavigateToEditMetric("Body Fat") }
                            )
                            
                            // Waist measurement
                            MetricRowWithArrow(
                                title = "Waist",
                                value = viewModel.getLatestHistoryEntry("Waist", "cm")?.toString() ?: "No Data",
                                unit = "cm",
                                onClick = { onNavigateToEditMetric("Waist") }
                            )
                            
                            // Bicep measurement
                            MetricRowWithArrow(
                                title = "Bicep",
                                value = viewModel.getLatestHistoryEntry("Bicep", "cm")?.toString() ?: "No Data",
                                unit = "cm",
                                onClick = { onNavigateToEditMetric("Bicep") }
                            )
                            
                            // Chest measurement
                            MetricRowWithArrow(
                                title = "Chest",
                                value = viewModel.getLatestHistoryEntry("Chest", "cm")?.toString() ?: "No Data",
                                unit = "cm",
                                onClick = { onNavigateToEditMetric("Chest") }
                            )
                            
                            // Thigh measurement
                            MetricRowWithArrow(
                                title = "Thigh",
                                value = viewModel.getLatestHistoryEntry("Thigh", "cm")?.toString() ?: "No Data",
                                unit = "cm",
                                onClick = { onNavigateToEditMetric("Thigh") }
                            )
                            
                            // Shoulder measurement
                            MetricRowWithArrow(
                                title = "Shoulder",
                                value = viewModel.getLatestHistoryEntry("Shoulder", "cm")?.toString() ?: "No Data",
                                unit = "cm",
                                onClick = { onNavigateToEditMetric("Shoulder") }
                            )
                        }
                    }
                }
                
                // CALCULATED Section
                item {
                    Text(
                        text = "CALCULATED",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A1A1A),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // BMI
                            MetricRowWithArrow(
                                title = "BMI",
                                value = formattedBmi,
                                unit = "",
                                onClick = { onNavigateToViewBMIHistory() }
                            )
                            
                            // Get calculated metrics
                            val calculatedMetrics = viewModel.getCalculatedMetrics()
                            
                            // Lean Body Mass
                            MetricRowWithArrow(
                                title = "Lean Body Mass",
                                value = calculatedMetrics["Lean Body Mass"]?.let { 
                                    String.format("%.1f", it).let { 
                                        if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                    }
                                } ?: "No Data",
                                unit = "kg",
                                onClick = { /* No action */ }
                            )
                            
                            // Fat Mass
                            MetricRowWithArrow(
                                title = "Fat Mass",
                                value = calculatedMetrics["Fat Mass"]?.let { 
                                    String.format("%.1f", it).let { 
                                        if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                    }
                                } ?: "No Data",
                                unit = "kg",
                                onClick = { /* No action */ }
                            )
                            
                            // Fat-Free Mass Index
                            MetricRowWithArrow(
                                title = "Fat-Free Mass Index",
                                value = calculatedMetrics["Fat-Free Mass Index"]?.let { 
                                    String.format("%.1f", it).let { 
                                        if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                                    }
                                } ?: "No Data",
                                unit = "",
                                onClick = { /* No action */ }
                            )
                            
                            // Basal Metabolic Rate
                            MetricRowWithArrow(
                                title = "Basal Metabolic Rate",
                                value = calculatedMetrics["Basal Metabolic Rate"]?.let { 
                                    String.format("%.0f", it)
                                } ?: "No Data",
                                unit = "kcal",
                                onClick = { /* No action */ }
                            )
                            
                            // Body Surface Area
                            MetricRowWithArrow(
                                title = "Body Surface Area",
                                value = calculatedMetrics["Body Surface Area"]?.let { 
                                    String.format("%.2f", it).let { 
                                        if (it.endsWith(".00")) it.substring(0, it.length - 3) else it 
                                    }
                                } ?: "No Data",
                                unit = "m²",
                                onClick = { /* No action */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRowWithArrow(
    title: String,
    value: String,
    unit: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (unit.isEmpty()) value else "$value $unit",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Edit $title",
                modifier = Modifier.padding(start = 8.dp),
                tint = Color.White
            )
        }
    }
    Divider(color = Color(0xFF333333), thickness = 0.5.dp)
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
            if (unit.isNotEmpty() && value != "No Data") {
                Text(
                    text = " $unit",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }
}