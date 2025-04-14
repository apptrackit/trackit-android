package com.example.lifetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifetracker.data.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MetricHistoryChart(
    history: List<HistoryEntry>,
    unit: String
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "-",
                color = Color(0xFF444444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        return
    }

    val sortedHistory = history.sortedBy { it.date }
    val minValue = sortedHistory.minOf { it.value }
    val maxValue = sortedHistory.maxOf { it.value }
    
    // Add padding to min and max values for better visualization
    val paddedMinValue = (minValue - (maxValue - minValue) * 0.1f).coerceAtLeast(0f)
    val paddedMaxValue = maxValue + (maxValue - minValue) * 0.1f
    
    val valueRange = paddedMaxValue - paddedMinValue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val xStep = if (sortedHistory.size > 1) width / (sortedHistory.size - 1) else width

            // Draw grid lines
            val gridLineColor = Color(0xFF333333)
            val gridLineStrokeWidth = 1.dp.toPx()
            
            // Horizontal grid lines (5 lines)
            for (i in 0..4) {
                val y = height * (1 - i / 4f)
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = gridLineStrokeWidth
                )
            }

            // Draw axis
            drawLine(Color.Gray, Offset(0f, height), Offset(width, height), strokeWidth = 2.dp.toPx())
            drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, height), strokeWidth = 2.dp.toPx())

            // Draw data points and lines
            val path = Path()
            sortedHistory.forEachIndexed { index, entry ->
                val x = if (sortedHistory.size > 1) index * xStep else width / 2
                val normalizedValue = (entry.value - paddedMinValue) / valueRange
                val y = height - normalizedValue * height * 0.9f // Leave some margin at the top

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw data point with a larger circle and a white border
                drawCircle(
                    color = Color(0xFF2196F3),
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(x, y),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw the line with a gradient effect
            drawPath(
                path = path,
                color = Color(0xFF2196F3),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Draw value labels on the y-axis
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format("%.1f", paddedMaxValue).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                },
                color = Color.Gray,
                fontSize = 10.sp
            )
            Text(
                text = String.format("%.1f", (paddedMaxValue + paddedMinValue) / 2).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                },
                color = Color.Gray,
                fontSize = 10.sp
            )
            Text(
                text = String.format("%.1f", paddedMinValue).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                },
                color = Color.Gray,
                fontSize = 10.sp
            )
        }

        // Draw date labels below the chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(top = 210.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            if (sortedHistory.size == 1) {
                // Center the single date
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = dateFormat.format(Date(sortedHistory[0].date)),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                // Show only first, middle, and last dates for better readability
                val firstDate = dateFormat.format(Date(sortedHistory.first().date))
                val lastDate = dateFormat.format(Date(sortedHistory.last().date))
                
                // If there are more than 2 entries, show the middle date
                if (sortedHistory.size > 2) {
                    val middleIndex = sortedHistory.size / 2
                    val middleDate = dateFormat.format(Date(sortedHistory[middleIndex].date))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = firstDate,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = middleDate,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = lastDate,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // Just show first and last dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = firstDate,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = lastDate,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}