package com.example.lifetracker.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.lifetracker.ui.components.ClickableMetricCard
import com.example.lifetracker.ui.components.MetricCard
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI

@SuppressLint("DefaultLocale")
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: HealthViewModel
) {
    // Get latest values from history
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Use current metrics as fallback if history is empty
    val metrics = viewModel.metrics
    val weightValue = latestWeight ?: 0f
    val heightValue = latestHeight ?: 0f
    val bodyFatValue = latestBodyFat ?: 0f

    // Calculate BMI with null safety
    val bmi = if (weightValue > 0 && heightValue > 0) {
        calculateBMI(weightValue, heightValue)
    } else {
        0f
    }

    val formattedBmi = if (bmi > 0) {
        val formatted = String.format("%.1f", bmi)
        if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
    } else "No Data"
    
    val formattedWeight = if (weightValue > 0) {
        val formatted = String.format("%.1f", weightValue)
        if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
    } else "No Data"
    
    val formattedHeight = if (heightValue > 0) heightValue.toInt().toString() else "No Data"
    
    val formattedBodyFat = if (bodyFatValue > 0) {
        val formatted = String.format("%.1f", bodyFatValue)
        if (formatted.endsWith(".0")) formatted.substring(0, formatted.length - 2) else formatted
    } else "No Data"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dashboard",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ClickableMetricCard(
                        title = "Weight",
                        value = formattedWeight,
                        unit = "kg",
                        onClick = { navController.navigate("edit_weight") },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCard(
                        title = "Height",
                        value = formattedHeight,
                        unit = "cm",
                        onClick = { navController.navigate("edit_height") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        title = "BMI",
                        value = formattedBmi,
                        unit = "",
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCard(
                        title = "Body Fat",
                        value = formattedBodyFat,
                        unit = "%",
                        onClick = { navController.navigate("edit_body_fat") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
