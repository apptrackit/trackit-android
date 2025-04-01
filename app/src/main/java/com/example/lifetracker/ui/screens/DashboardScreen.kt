package com.example.lifetracker.ui.screens

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
import androidx.navigation.NavHostController
import com.example.lifetracker.ui.components.ClickableMetricCard
import com.example.lifetracker.ui.components.MetricCard
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: HealthViewModel
) {
    val metrics = viewModel.metrics

    // Calculate BMI
    val bmi = calculateBMI(metrics.weight, metrics.height)
    val formattedBmi = String.format("%.1f", bmi)

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
                        value = String.format("%.1f", metrics.weight),
                        unit = "kg",
                        onClick = { navController.navigate("edit_weight") },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCard(
                        title = "Height",
                        value = metrics.height.toInt().toString(),
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
                        value = String.format("%.1f", metrics.bodyFat),
                        unit = "%",
                        onClick = { navController.navigate("edit_bodyfat") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
