package com.ballabotond.trackit.ui.screens.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ballabotond.trackit.ui.components.AddMetricPopup
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.utils.calculateBMI
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.ballabotond.trackit.data.model.SyncState
import com.ballabotond.trackit.ui.components.MetricCardRedesignedWithFaIcon
import com.ballabotond.trackit.ui.components.RecentMeasurementRow
import com.ballabotond.trackit.ui.components.SmoothMetricChart
import com.ballabotond.trackit.ui.components.TimeFilterButton
import com.ballabotond.trackit.ui.theme.IconChoose
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun DashboardScreen(
    onNavigateToEditMetric: (String) -> Unit,
    onNavigateToViewBMIHistory: () -> Unit,
    viewModel: HealthViewModel,
    navController: NavController,
    syncViewModel: com.ballabotond.trackit.ui.viewmodel.SyncViewModel? = null
) {
    // State for showing the popup
    var showAddMetricPopup by remember { mutableStateOf(false) }

    // Sync state
    val syncState by syncViewModel?.syncState?.collectAsState() ?: remember { mutableStateOf(SyncState()) }

    // Pull to refresh state
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

    // Time filter state (must be before usage)
    var selectedTimeFilter by remember { mutableStateOf("6M") }

    // Get latest values from history
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Get history data for charts
    val weightHistory = viewModel.getFilteredMetricHistory("Weight", "kg", selectedTimeFilter)
    val heightHistory = viewModel.getFilteredMetricHistory("Height", "cm", selectedTimeFilter)
    val bodyFatHistory = viewModel.getFilteredMetricHistory("Body Fat", "%", selectedTimeFilter)
    val bmiHistory = viewModel.getFilteredMetricHistory("BMI", "", selectedTimeFilter)

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

    val context = LocalContext.current
    val now = remember { Calendar.getInstance() }
    val greeting = remember {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val dateString = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        sdf.format(now.time)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF222222))
                                    .clickable { navController.navigate("profile") }
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Dashboard",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.background(Color(0xFF222222), RoundedCornerShape(50.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (syncViewModel != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp)
                                ) {
                                    if (syncState.isSyncing) {
                                        Text(text = "Syncing...", color = Color.White, fontSize = 14.sp)
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color(0xFF4CAF50), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Synced", color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                            IconButton(onClick = { showAddMetricPopup = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    // Greeting Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text(
                            text = greeting,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateString,
                            color = Color(0xFFAAAAAA),
                            fontSize = 15.sp
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    // Metric Cards 2x2 grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCardRedesignedWithFaIcon(
                                title = "Weight",
                                value = formattedWeight,
                                unit = "kg",
                                icon = IconChoose.getIcon("Weight").first,
                                iconTint = IconChoose.getIcon("Weight").second,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToEditMetric("Weight") }
                            )
                            MetricCardRedesignedWithFaIcon(
                                title = "Body Fat",
                                value = formattedBodyFat,
                                unit = "%",
                                icon = IconChoose.getIcon("Body Fat").first,
                                iconTint = IconChoose.getIcon("Body Fat").second,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToEditMetric("Body Fat") }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCardRedesignedWithFaIcon(
                                title = "BMI",
                                value = formattedBmi,
                                unit = "",
                                icon = IconChoose.getIcon("BMI").first,
                                iconTint = IconChoose.getIcon("BMI").second,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToViewBMIHistory() }
                            )
                            MetricCardRedesignedWithFaIcon(
                                title = "Height",
                                value = formattedHeight,
                                unit = "cm",
                                icon = IconChoose.getIcon("Height").first,
                                iconTint = IconChoose.getIcon("Height").second,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToEditMetric("Height") }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item {
                    // Progress Section
                    Text(
                        text = "Progress",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 18.dp, bottom = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeFilterButton("W", selectedTimeFilter == "W") { selectedTimeFilter = "W" }
                        TimeFilterButton("M", selectedTimeFilter == "M") { selectedTimeFilter = "M" }
                        TimeFilterButton("6M", selectedTimeFilter == "6M") { selectedTimeFilter = "6M" }
                        TimeFilterButton("Y", selectedTimeFilter == "Y") { selectedTimeFilter = "Y" }
                        TimeFilterButton("All", selectedTimeFilter == "All") { selectedTimeFilter = "All" }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // Trend Charts
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181818), RoundedCornerShape(18.dp))
                                .padding(10.dp)
                                .clickable { onNavigateToEditMetric("Weight") }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Weight Trend", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${formattedWeight}kg", color = Color.White, fontSize = 14.sp)
                                }
                                SmoothMetricChart(history = weightHistory, unit = "kg")
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181818), RoundedCornerShape(18.dp))
                                .padding(10.dp)
                                .clickable { onNavigateToEditMetric("Body Fat") }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Body Fat Trend", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${formattedBodyFat}%", color = Color.White, fontSize = 14.sp)
                                }
                                SmoothMetricChart(history = bodyFatHistory, unit = "%")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item {
                    // Recent Measurements
                    Text(
                        text = "Recent Measurements",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 18.dp, bottom = 8.dp)
                    )
                }

                // Recent Measurements List
                val recent = viewModel.getRecentMeasurements(5)
                items(recent) { entry ->
                    RecentMeasurementRow(
                        entry = entry,
                        onClick = { onNavigateToEditMetric(entry.metricName) }
                    )
                }
            }

            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF333333),
                contentColor = Color.White
            )
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
}

