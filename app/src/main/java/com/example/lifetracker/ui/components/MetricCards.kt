package com.example.lifetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.theme.FontAwesomeIcon
import com.guru.fontawesomecomposelib.FaIconType

@Composable
fun ClickableMetricCard(
    title: String,
    value: String,
    unit: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        MetricCardContent(title, value, unit)
    }
}

@Composable
fun ClickableMetricCardWithChart(
    title: String,
    value: String,
    unit: String,
    history: List<HistoryEntry>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        MetricCardContentWithChart(title, value, unit, history)
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        MetricCardContent(title, value, unit)
    }
}

@Composable
fun MetricCardContent(title: String, value: String, unit: String) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .height(80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                color = if (value == "-") Color(0xFF555555) else Color.White,
                fontSize = if (value == "-") 14.sp else 32.sp,
                fontWeight = if (value == "-") FontWeight.Normal else FontWeight.Bold,
                modifier = Modifier.padding(bottom = if (value == "-") 0.dp else 2.dp)
            )
            if (unit.isNotEmpty() && value != "-") {
                Text(
                    text = unit,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MetricCardContentWithChart(title: String, value: String, unit: String, history: List<HistoryEntry>) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .height(80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        if (history.isEmpty() || value == "No Data") {
            // If no history, just show the value without a chart
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    color = if (value == "No Data") Color(0xFF555555) else Color.White,
                    fontSize = if (value == "No Data") 14.sp else 32.sp,
                    fontWeight = if (value == "No Data") FontWeight.Normal else FontWeight.Bold,
                    modifier = Modifier.padding(bottom = if (value == "No Data") 0.dp else 2.dp)
                )
                if (unit.isNotEmpty() && value != "No Data") {
                    Text(
                        text = unit,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }
            }
        } else {
            // Show value and chart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Value
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }
                
                // Chart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .padding(start = 8.dp)
                ) {
                    MetricCardChart(history = history)
                }
            }
        }
    }
}

@Composable
fun MetricCardChart(history: List<HistoryEntry>) {
    if (history.isEmpty()) return
    
    // Filter for monthly data
    val monthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
    val monthlyHistory = history.filter { it.date >= monthAgo }.sortedBy { it.date }
    
    if (monthlyHistory.isEmpty()) return
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        
        // Calculate min and max values for scaling
        val minValue = monthlyHistory.minOf { it.value }
        val maxValue = monthlyHistory.maxOf { it.value }
        val range = (maxValue - minValue).coerceAtLeast(0.1f)
        val paddedMin = (minValue - range * 0.1f).coerceAtLeast(0f)
        val paddedMax = maxValue + range * 0.1f
        val valueRange = paddedMax - paddedMin
        
        // Draw the smooth chart
        if (monthlyHistory.size > 1) {
            val path = Path()
            val points = monthlyHistory.mapIndexed { index, entry ->
                val x = index * width / (monthlyHistory.size - 1)
                val normalizedValue = (entry.value - paddedMin) / valueRange
                val y = height - normalizedValue * height * 0.9f
                Offset(x, y)
            }
            
            // Start path
            path.moveTo(points[0].x, points[0].y)
            
            // Draw smooth curve through points
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val current = points[i]
                
                // Simple cubic interpolation for smoothing
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
            
            // Draw the smooth path
            drawPath(
                path = path,
                color = Color(0x4D2196F3), // Semi-transparent blue
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        } else if (monthlyHistory.size == 1) {
            // For single point, draw a horizontal line
            val entry = monthlyHistory[0]
            val normalizedValue = (entry.value - paddedMin) / valueRange
            val y = height - normalizedValue * height * 0.9f
            
            drawLine(
                color = Color(0x4D2196F3),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
fun MetricCardRedesigned(
    title: String,
    value: String,
    unit: String,
    icon: Painter,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (unit.isNotEmpty()) {
                        Text(
                            text = unit,
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 3.dp, bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCardRedesignedWithFaIcon(
    title: String,
    value: String,
    unit: String,
    icon: FaIconType.SolidIcon,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FontAwesomeIcon(
                    icon = icon,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp).padding(0.dp) // Remove any implicit padding
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                // Different display style for "No Data" versus actual measurements
                if (value == "No Data") {
                    Text(
                        text = value,
                        color = Color(0xFF666666), // Darker gray for "No Data"
                        fontSize = 16.sp, // Smaller font size
                        fontWeight = FontWeight.Normal // Not bold
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unit.isNotEmpty()) {
                            Text(
                                text = unit,
                                color = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 3.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
