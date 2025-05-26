package com.example.lifetracker.ui.screens.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import com.guru.fontawesomecomposelib.FaIcons
import com.guru.fontawesomecomposelib.FaIconType
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
import com.example.lifetracker.ui.components.AddMetricPopup
import com.example.lifetracker.ui.components.ClickableMetricCardWithChart
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.calculateBMI
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.components.MetricCardRedesigned
import com.example.lifetracker.ui.components.MetricCardRedesignedWithFaIcon
import com.example.lifetracker.ui.components.MetricHistoryChart
import com.example.lifetracker.ui.theme.FontAwesomeIcon
import java.text.SimpleDateFormat
import java.util.*

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
        val sdf = SimpleDateFormat("yyyy. MMM dd., EEEE", Locale.getDefault())
        sdf.format(now.time)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
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
                        modifier = Modifier.clickable { navController.navigate("profile") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222222))
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
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
                    IconButton(onClick = { showAddMetricPopup = true }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_input_add),
                            contentDescription = "Add",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                            icon = FaIcons.Weight,
                            iconTint = Color(0xFF2196F3),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToEditMetric("Weight") }
                        )
                        MetricCardRedesignedWithFaIcon(
                            title = "Body Fat",
                            value = formattedBodyFat,
                            unit = "%",
                            icon = FaIcons.Percent,
                            iconTint = Color(0xFF4CAF50),
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
                            icon = FaIcons.User,
                            iconTint = Color(0xFFFF9800),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToViewBMIHistory() }
                        )
                        MetricCardRedesignedWithFaIcon(
                            title = "Height",
                            value = formattedHeight,
                            unit = "cm",
                            icon = FaIcons.RulerVertical,
                            iconTint = Color(0xFF9C27B0),
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

@Composable
fun RecentMeasurementRow(entry: HistoryEntry, onClick: () -> Unit) {
    val iconAndColor = when (entry.metricName) {
        "Weight" -> Pair(FaIcons.Weight, Color(0xFF2196F3))
        "Body Fat" -> Pair(FaIcons.Percent, Color(0xFF4CAF50))
        "Thigh" -> Pair(FaIcons.Child, Color(0xFF00BCD4))
        "Bicep" -> Pair(FaIcons.Dumbbell, Color(0xFFFF9800))
        "Height" -> Pair(FaIcons.RulerVertical, Color(0xFF9C27B0))
        "BMI" -> Pair(FaIcons.User, Color(0xFFFF9800))
        else -> Pair(FaIcons.QuestionCircle, Color(0xFFAAAAAA))
    }
    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(Color(0xFF181818), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .size(28.dp)
                .background(iconAndColor.second.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            FontAwesomeIcon(
                icon = iconAndColor.first,
                tint = iconAndColor.second,
                modifier = Modifier
                    .size(20.dp)
                    .padding(0.dp) // Remove any implicit padding
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.metricName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = dateFormat.format(Date(entry.date)),
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${entry.value} ${entry.unit}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun TimeFilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF4CAF50) else Color(0xFF222222),
            contentColor = if (selected) Color.White else Color(0xFFAAAAAA)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SmoothMetricChart(history: List<HistoryEntry>, unit: String, modifier: Modifier = Modifier) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Data",
                color = Color(0xFF444444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
        return
    }

    val sortedHistory = history.sortedBy { it.date }
    val minValue = sortedHistory.minOf { it.value }
    val maxValue = sortedHistory.maxOf { it.value }
    
    // Add padding to the range to prevent points at edges
    val range = (maxValue - minValue).coerceAtLeast(0.1f)
    val paddedMin = (minValue - range * 0.1f).coerceAtLeast(0f)
    val paddedMax = maxValue + range * 0.1f
    val valueRange = paddedMax - paddedMin

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 45.dp, end = 10.dp, top = 10.dp, bottom = 25.dp) // Increased left padding from 35dp to 45dp
        ) {
            val width = size.width
            val height = size.height
            
            // Draw y-axis grid lines and labels
            val ySteps = 4
            for (i in 0..ySteps) {
                val y = height - (i.toFloat() / ySteps.toFloat() * height)
                
                // Draw horizontal grid line
                drawLine(
                    color = Color(0xFF333333),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Calculate and draw the value for this grid line
                val value = paddedMin + (i.toFloat() / ySteps.toFloat() * valueRange)
                val formattedValue = String.format("%.1f", value).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                }
                
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        formattedValue,
                        -40.dp.toPx(), // Increased from -30dp to -40dp to move labels further left
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 8.sp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }
            }
            
            // Draw smooth path
            if (sortedHistory.size > 1) {
                val path = Path()
                val points = sortedHistory.mapIndexed { index, entry ->
                    val x = index * width / (sortedHistory.size - 1)
                    val normalizedValue = (entry.value - paddedMin) / valueRange
                    val y = height - normalizedValue * height
                    Offset(x, y)
                }
                
                path.moveTo(points[0].x, points[0].y)
                
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val current = points[i]
                    
                    val controlX1 = prev.x + (current.x - prev.x) / 3
                    val controlY1 = prev.y
                    val controlX2 = current.x - (current.x - prev.x) / 3
                    val controlY2 = current.y
                    
                    path.cubicTo(
                        controlX1, controlY1,
                        controlX2, controlY2,
                        current.x, current.y
                    )
                }
                
                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            } else if (sortedHistory.size == 1) {
                // For single point, draw a horizontal line
                val entry = sortedHistory[0]
                val normalizedValue = (entry.value - paddedMin) / valueRange
                val y = height - normalizedValue * height
                
                drawLine(
                    color = Color(0xFF2196F3),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Draw x-axis date labels
            if (sortedHistory.size > 1) {
                val dateFormat = SimpleDateFormat("d", Locale.getDefault())
                val labelPositions = if (sortedHistory.size <= 5) {
                    sortedHistory.indices.toList() // Show all dates if 5 or fewer
                } else {
                    // Show first, middle, and last for more than 5 points
                    listOf(0, sortedHistory.size / 2, sortedHistory.lastIndex)
                }
                
                labelPositions.forEach { index ->
                    val x = index * width / (sortedHistory.size - 1)
                    val date = Date(sortedHistory[index].date)
                    
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            dateFormat.format(date),
                            x,
                            height + 20.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 8.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}
