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

@SuppressLint("DefaultLocale")
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: HealthViewModel
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
            if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
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
            if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
        } else "No Data"
    }

    // Calculate BMI with null safety
    val bmi = if (weightHistory.isNotEmpty() && heightHistory.isNotEmpty() && 
                 latestWeight != null && latestHeight != null && 
                 latestWeight > 0 && latestHeight > 0) {
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
                    text = "Dashboard",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                AddMetricButton(
                    onClick = { showAddMetricPopup = true }
                )
            }

            // Metric cards grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ClickableMetricCardWithChart(
                        title = "Weight",
                        value = formattedWeight,
                        unit = "kg",
                        history = weightHistory,
                        onClick = { navController.navigate("edit_weight") },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCardWithChart(
                        title = "Height",
                        value = formattedHeight,
                        unit = "cm",
                        history = heightHistory,
                        onClick = { navController.navigate("edit_height") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ClickableMetricCardWithChart(
                        title = "BMI",
                        value = formattedBmi,
                        unit = "",
                        history = bmiHistory,
                        onClick = { navController.navigate("view_bmi_history") },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCardWithChart(
                        title = "Body Fat",
                        value = formattedBodyFat,
                        unit = "%",
                        history = bodyFatHistory,
                        onClick = { navController.navigate("edit_body_fat") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    // Show the popup when showAddMetricPopup is true
    if (showAddMetricPopup) {
        AddMetricPopup(
            onDismiss = { showAddMetricPopup = false },
            navController = navController
        )
    }
}
