package com.example.lifetracker.ui.screens.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.guru.fontawesomecomposelib.FaIcons
import com.guru.fontawesomecomposelib.FaIconType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.ui.components.AddMetricButton
import com.example.lifetracker.ui.components.AddMetricPopup
import com.example.lifetracker.ui.components.ClickableMetricCardWithChart
import com.example.lifetracker.ui.components.StepCountCard
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI
import com.example.lifetracker.data.repository.StepCountRepository

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
    
    // Get latest values from history
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Get history data for charts
    val weightHistory = viewModel.getMetricHistory("Weight", "kg")
    val heightHistory = viewModel.getMetricHistory("Height", "cm")
    val bodyFatHistory = viewModel.getMetricHistory("Body Fat", "%")
    val bmiHistory = viewModel.getMetricHistory("BMI", "")
    
    // Get step count
    val stepCount = viewModel.todayStepCount
    val stepCountLoading = viewModel.stepCountLoading
    val weeklyStepCounts = viewModel.weeklyStepCounts
    
    // Handle permission requests
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Check for permissions and subscribe on first composition
    LaunchedEffect(Unit) {
        if (viewModel.hasGoogleFitPermissions()) {
            Log.d("DashboardScreen", "Have Google Fit permissions on launch")
            // Already have permissions, fetch data and subscribe to updates
            viewModel.subscribeToStepCountUpdates()
            viewModel.refreshTodayStepCount()
            viewModel.refreshWeeklyStepCounts()
        } else {
            Log.d("DashboardScreen", "No Google Fit permissions on launch")
        }
    }
    
    // When permission status changes, refresh data if we now have permissions
    LaunchedEffect(viewModel.permissionRequestInProgress) {
        if (!viewModel.permissionRequestInProgress && viewModel.hasGoogleFitPermissions()) {
            Log.d("DashboardScreen", "Permission request completed, have permissions")
            viewModel.subscribeToStepCountUpdates()
            viewModel.refreshTodayStepCount()
            viewModel.refreshWeeklyStepCounts()
        }
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
            StepCountCard(
                steps = stepCount,
                isLoading = stepCountLoading,
                weeklySteps = weeklyStepCounts,
                onClick = {
                    if (!viewModel.hasGoogleFitPermissions()) {
                        Log.d("DashboardScreen", "No permissions, requesting Google Fit permissions")
                        viewModel.resetPermissionRequestState() // Reset before requesting
                        activity?.let {
                            viewModel.requestGoogleFitPermissions(it)
                        }
                    } else {
                        Log.d("DashboardScreen", "Have permissions, refreshing step count")
                        viewModel.refreshTodayStepCount()
                        viewModel.refreshWeeklyStepCounts()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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
