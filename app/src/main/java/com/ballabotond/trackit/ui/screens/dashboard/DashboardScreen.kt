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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.ballabotond.trackit.data.model.SyncState
import com.ballabotond.trackit.ui.components.RecentMeasurementRow
import com.ballabotond.trackit.ui.components.SmoothMetricChart
import com.ballabotond.trackit.ui.theme.FeatherIcon
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection
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
    syncViewModel: com.ballabotond.trackit.ui.viewmodel.SyncViewModel? = null,
    onNavigateToPhotos: () -> Unit = {},
    onLaunchGallery: () -> Unit = {}
) {
    var showAddMetricPopup by remember { mutableStateOf(false) }
    val syncState by syncViewModel?.syncState?.collectAsState() ?: remember { mutableStateOf(SyncState()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            syncViewModel?.performSync()
        }
    )

    LaunchedEffect(syncState.isSyncing) {
        if (!syncState.isSyncing) {
            isRefreshing = false
        }
    }

    var selectedTimeFilter by remember { mutableStateOf("6M") }

    // Get latest values
    val latestWeight = viewModel.getLatestHistoryEntry("Weight", "kg")
    val latestHeight = viewModel.getLatestHistoryEntry("Height", "cm")
    val latestBodyFat = viewModel.getLatestHistoryEntry("Body Fat", "%")

    // Get history data for charts
    val weightHistory = viewModel.getFilteredMetricHistory("Weight", "kg", selectedTimeFilter)
    val bodyFatHistory = viewModel.getFilteredMetricHistory("Body Fat", "%", selectedTimeFilter)

    // Calculate BMI
    val bmi = if (latestWeight != null && latestHeight != null && latestWeight > 0 && latestHeight > 0) {
        calculateBMI(latestWeight, latestHeight)
    } else 0f

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
        val sdf = SimpleDateFormat("yyyy. MMMM d., EEEE", Locale.getDefault())
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF2C2C2E), CircleShape)
                                    .clickable { navController.navigate("profile") },
                                contentAlignment = Alignment.Center
                            ) {
                                FeatherIcon(
                                    icon = FeatherIconsCollection.User,
                                    tint = Color.White,
                                    size = 20.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Dashboard",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .background(Color(0xFF2C2C2E), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!syncState.isSyncing) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF30D158), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Synced",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "Syncing...",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { 
                                        navController.navigate("add_metric_data/Weight/kg/Weight")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                FeatherIcon(
                                    icon = FeatherIconsCollection.Plus,
                                    tint = Color.White,
                                    size = 18.dp
                                )
                            }
                        }
                    }
                }

                item {
                    // Greeting exactly like in image
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = greeting,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateString,
                            color = Color(0xFF8E8E93),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                item {
                    // Metric Cards 2x2 grid exactly like in image
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MetricCard(
                                title = "Weight",
                                value = latestWeight?.let { "%.1f".format(it).replace(".0", "") } ?: "5.0",
                                unit = "kg",
                                icon = FeatherIconsCollection.Target,
                                iconColor = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToEditMetric("Weight") }
                            )
                            MetricCard(
                                title = "Body Fat",
                                value = latestBodyFat?.let { "%.1f".format(it).replace(".0", "") } ?: "13.4",
                                unit = "%",
                                icon = FeatherIconsCollection.User,
                                iconColor = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToEditMetric("Body Fat") }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MetricCard(
                                title = "BMI",
                                value = if (bmi > 0) "%.1f".format(bmi).replace(".0", "") else "1.6",
                                unit = "",
                                icon = FeatherIconsCollection.Activity,
                                iconColor = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToViewBMIHistory() }
                            )
                            MetricCard(
                                title = "Height",
                                value = latestHeight?.let { "%.0f".format(it) } ?: "175.0",
                                unit = "cm",
                                icon = FeatherIconsCollection.Ruler,
                                iconColor = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToEditMetric("Height") }
                            )
                        }
                    }
                }

                item {
                    // Progress section exactly like in image
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            TimeFilterChip("W", selectedTimeFilter == "W") { selectedTimeFilter = "W" }
                            TimeFilterChip("M", selectedTimeFilter == "M") { selectedTimeFilter = "M" }
                            TimeFilterChip("6M", selectedTimeFilter == "6M") { selectedTimeFilter = "6M" }
                            TimeFilterChip("Y", selectedTimeFilter == "Y") { selectedTimeFilter = "Y" }
                            TimeFilterChip("All", selectedTimeFilter == "All") { selectedTimeFilter = "All" }
                        }
                    }
                }

                item {
                    // Trend Charts exactly like in image
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TrendChart(
                            title = "Weight Trend",
                            value = latestWeight?.let { "%.1f".format(it).replace(".0", "") } ?: "5.0",
                            unit = "kg",
                            history = weightHistory,
                            modifier = Modifier.clickable { onNavigateToEditMetric("Weight") }
                        )
                        
                        TrendChart(
                            title = "Body Fat Trend",
                            value = latestBodyFat?.let { "%.1f".format(it).replace(".0", "") } ?: "13.4",
                            unit = "%",
                            history = bodyFatHistory,
                            modifier = Modifier.clickable { onNavigateToEditMetric("Body Fat") }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    // Recent Measurements section exactly like in image
                    Text(
                        text = "Recent Measurements",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Recent Measurements List - matching the design exactly
                val recentMeasurements = viewModel.getRecentMeasurements(3)
                items(recentMeasurements) { entry ->
                    RecentMeasurementRow(
                        entry = entry,
                        onClick = { onNavigateToEditMetric(entry.metricName) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    // Quick Actions section exactly like in image
                    Text(
                        text = "Quick Actions",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    // Quick Actions buttons exactly like in image
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionCard(
                            title = "Add Metric",
                            icon = FeatherIconsCollection.Ruler,
                            iconColor = Color(0xFF4CAF50),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showAddMetricPopup = true }
                        )
                        QuickActionCard(
                            title = "Add Photo",
                            icon = FeatherIconsCollection.Camera,
                            iconColor = Color(0xFF4CAF50),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onLaunchGallery() }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF2C2C2E),
                contentColor = Color.White
            )
        }

        if (showAddMetricPopup) {
            AddMetricPopup(
                onDismiss = { showAddMetricPopup = false },
                onNavigateToEditMetric = onNavigateToEditMetric,
                navController = navController
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.6f / 1f) // Slightly taller than 2:1 ratio
            .background(Color(0xFF2C2C2E), RoundedCornerShape(20.dp))
            .padding(18.dp) // Slightly more padding for better text fit
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween // Better distribution
        ) {
            // Top row with icon and title - matching Swift HStack
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FeatherIcon(
                    icon = icon,
                    tint = iconColor,
                    size = 18.dp // Slightly larger icon
                )
                Text(
                    text = title,
                    color = Color(0xFF8E8E93), // Gray color like Swift .gray
                    fontSize = 15.sp, // Slightly larger for better readability
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            
            // Bottom value and unit - matching Swift HStack with baseline alignment
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    color = Color.White, // Primary color
                    fontSize = 26.sp, // Adjusted size for better fit
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        color = Color(0xFF8E8E93), // Gray color
                        fontSize = 15.sp, // Slightly larger for better readability
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 3.dp), // Better baseline alignment
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Color(0xFF2C2C2E) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF8E8E93),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun TrendChart(
    title: String,
    value: String,
    unit: String,
    history: List<com.ballabotond.trackit.data.model.HistoryEntry>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$value$unit",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart using SmoothMetricChart component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF1C1C1E), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (history.isNotEmpty()) {
                    SmoothMetricChart(
                        history = history,
                        unit = unit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data available",
                            color = Color(0xFF8E8E93),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .background(Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FeatherIcon(
                    icon = icon,
                    tint = iconColor,
                    size = 24.dp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

