package com.example.lifetracker.ui.screens.dashboard

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.lifetracker.ui.components.AddMetricButton
import com.example.lifetracker.ui.components.AddMetricPopup
import com.example.lifetracker.ui.components.ClickableMetricCard
import com.example.lifetracker.ui.components.ClickableMetricCardWithChart
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons

@SuppressLint("DefaultLocale")
@Composable
fun DashboardScreen(
    onNavigateToEditMetric: (String) -> Unit,
    onNavigateToViewBMIHistory: () -> Unit,
    viewModel: HealthViewModel,
    navController: NavController
) {
    // State for showing the popup
    var showAddMetricPopup by remember { mutableStateOf(false) }
    
    // Context for launching intents
    val context = LocalContext.current
    
    // Get latest values from history
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Get history data for charts
    val weightHistory = viewModel.getMetricHistory("Weight", "kg")
    val heightHistory = viewModel.getMetricHistory("Height", "cm")
    val bodyFatHistory = viewModel.getMetricHistory("Body Fat", "%")
    val bmiHistory = viewModel.getMetricHistory("BMI", "")

    // Collect step count from Health Connect
    val stepCount = viewModel.stepCount.collectAsStateWithLifecycle().value
    
    // Activity launcher for permission requests
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check permission status after coming back from permission screen
        viewModel.checkHealthConnectStatus()
    }

    // Effect to refresh step data when screen is shown
    LaunchedEffect(Unit) {
        viewModel.checkHealthConnectStatus()
    }

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
                .verticalScroll(rememberScrollState())
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

            // Step counter card
            Text(
                text = "TODAY'S ACTIVITY",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
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
                        Column {
                            Text(
                                text = "Steps",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            if (viewModel.healthConnectAvailable) {
                                if (viewModel.permissionsGranted) {
                                    Text(
                                        text = "$stepCount",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    // Show permission button
                                    TextButton(
                                        onClick = {
                                            try {
                                                val intent = viewModel.getPermissionRequestIntent()
                                                permissionLauncher.launch(intent)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Color(0xFF2196F3)
                                        )
                                    ) {
                                        Text("Grant Permission")
                                    }
                                }
                            } else {
                                // Health Connect not available
                                TextButton(
                                    onClick = {
                                        val intent = viewModel.getHealthConnectInstallIntent()
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFF2196F3)
                                    )
                                ) {
                                    Text("Install Health Connect")
                                }
                            }
                        }
                        
                        // Use FontAwesome for walking icon since Material DirectionsWalk is not found
                        FaIcon(
                            faIcon = FaIcons.Running,
                            tint = Color(0xFF2196F3),
                            size = 32.dp
                        )
                    }
                }
            }

            // BODY METRICS header
            Text(
                text = "BODY METRICS",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )

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
                        onClick = { onNavigateToEditMetric("Weight") },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCardWithChart(
                        title = "Height",
                        value = formattedHeight,
                        unit = "cm",
                        history = heightHistory,
                        onClick = { onNavigateToEditMetric("Height") },
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
                        onClick = { onNavigateToViewBMIHistory() },
                        modifier = Modifier.weight(1f)
                    )
                    ClickableMetricCardWithChart(
                        title = "Body Fat",
                        value = formattedBodyFat,
                        unit = "%",
                        history = bodyFatHistory,
                        onClick = { onNavigateToEditMetric("Body Fat") },
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
            onNavigateToEditMetric = onNavigateToEditMetric,
            navController = navController
        )
    }
}
