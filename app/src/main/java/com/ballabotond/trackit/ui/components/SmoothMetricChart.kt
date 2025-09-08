package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.data.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.*

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
                .padding(8.dp)
        ) {
            val width = size.width
            val height = size.height
            
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
        }
    }
}
