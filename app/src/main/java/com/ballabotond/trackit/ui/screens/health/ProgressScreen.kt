package com.ballabotond.trackit.ui.screens.health

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.utils.calculateBMI
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
// --- CHANGE 1: Import the new AppIcon ---
import com.ballabotond.trackit.ui.common.AppIcon
import com.ballabotond.trackit.ui.theme.IconChoose
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun ProgressScreen(
    onNavigateToEditMetric: (String) -> Unit,
    onNavigateToViewBMIHistory: () -> Unit,
    viewModel: HealthViewModel,
    navController: NavController,
    syncViewModel: com.ballabotond.trackit.ui.viewmodel.SyncViewModel? = null
) {
    // Sync state and pull to refresh
    val syncState by syncViewModel?.syncState?.collectAsState() ?: remember { mutableStateOf(com.ballabotond.trackit.data.model.SyncState()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            syncViewModel?.performSync()
        }
    )

    // Listen for sync completion to stop refreshing
    LaunchedEffect(syncState.isSyncing) {
        if (!syncState.isSyncing) {
            isRefreshing = false
        }
    }

    // Get latest values
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Format values
    val formattedWeight = latestWeight?.let { String.format("%.1f", it).removeSuffix(".0") } ?: "-"
    val formattedHeight = latestHeight?.toInt()?.toString() ?: "-"
    val formattedBodyFat = latestBodyFat?.let { String.format("%.1f", it).removeSuffix(".0") } ?: "-"

    // Calculate BMI
    val bmi = if (latestWeight != null && latestHeight != null && latestWeight > 0 && latestHeight > 0) {
        calculateBMI(latestWeight, latestHeight)
    } else 0f
    val formattedBmi = if (bmi > 0) String.format("%.1f", bmi).removeSuffix(".0") else "-"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Progress",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
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
                                MetricRowWithArrow("Weight", formattedWeight, "kg") { onNavigateToEditMetric("Weight") }
                                MetricRowWithArrow("Height", formattedHeight, "cm") { onNavigateToEditMetric("Height") }
                                MetricRowWithArrow("Body Fat", formattedBodyFat, "%") { onNavigateToEditMetric("Body Fat") }
                                MetricRowWithArrow("Waist", viewModel.getLatestHistoryEntry("Waist", "cm")?.toString() ?: "-", "cm") { onNavigateToEditMetric("Waist") }
                                MetricRowWithArrow("Bicep", viewModel.getLatestHistoryEntry("Bicep", "cm")?.toString() ?: "-", "cm") { onNavigateToEditMetric("Bicep") }
                                MetricRowWithArrow("Chest", viewModel.getLatestHistoryEntry("Chest", "cm")?.toString() ?: "-", "cm") { onNavigateToEditMetric("Chest") }
                                MetricRowWithArrow("Thigh", viewModel.getLatestHistoryEntry("Thigh", "cm")?.toString() ?: "-", "cm") { onNavigateToEditMetric("Thigh") }
                                MetricRowWithArrow("Shoulder", viewModel.getLatestHistoryEntry("Shoulder", "cm")?.toString() ?: "-", "cm") { onNavigateToEditMetric("Shoulder") }
                            }
                        }
                    }

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
                            val calculatedMetrics = viewModel.getCalculatedMetrics()
                            Column(modifier = Modifier.fillMaxWidth()) {
                                MetricRowWithArrow("BMI", formattedBmi, "") { onNavigateToViewBMIHistory() }
                                MetricRowWithArrow("Lean Body Mass", calculatedMetrics["Lean Body Mass"]?.let { String.format("%.1f", it).removeSuffix(".0") } ?: "-", "kg") { /* nav */ }
                                MetricRowWithArrow("Fat Mass", calculatedMetrics["Fat Mass"]?.let { String.format("%.1f", it).removeSuffix(".0") } ?: "-", "kg") { /* nav */ }
                                MetricRowWithArrow("Fat-Free Mass Index", calculatedMetrics["Fat-Free Mass Index"]?.let { String.format("%.1f", it).removeSuffix(".0") } ?: "-", "") { /* nav */ }
                                MetricRowWithArrow("BMR", calculatedMetrics["Basal Metabolic Rate"]?.let { String.format("%.0f", it) } ?: "-", "kcal") { /* nav */ }
                                MetricRowWithArrow("Body Surface Area", calculatedMetrics["Body Surface Area"]?.let { String.format("%.2f", it).removeSuffix(".00") } ?: "-", "m²") { /* nav */ }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF333333),
                contentColor = Color.White
            )
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
    // --- CHANGE 2: Use getVisuals() and destructure into the correct variable names ---
    val (iconName, iconTint) = IconChoose.getVisuals(title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // --- CHANGE 3: Replace FontAwesomeIcon with AppIcon ---
                AppIcon(
                    iconName = iconName,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp), // Adjusted size to fit well
                    contentDescription = "$title icon"
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (value == "-") value else if (unit.isEmpty()) value else "$value $unit",
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