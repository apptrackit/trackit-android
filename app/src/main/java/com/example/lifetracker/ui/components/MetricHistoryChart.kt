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
                text = "No Data",
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
    val valueRange = if (maxValue > minValue) maxValue - minValue + 0.1f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val xStep = if (sortedHistory.size > 1) width / (sortedHistory.size - 1) else width

            // Draw axis
            drawLine(Color.Gray, Offset(0f, height), Offset(width, height))
            drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, height))

            // Draw data points and lines
            val path = Path()
            sortedHistory.forEachIndexed { index, entry ->
                val x = if (sortedHistory.size > 1) index * xStep else width / 2
                val normalizedValue = (entry.value - minValue) / valueRange
                val y = height - normalizedValue * height * 0.9f

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                drawCircle(Color(0xFF2196F3), 4.dp.toPx(), Offset(x, y))
            }

            drawPath(path, Color(0xFF2196F3), style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
        }

        // Draw date labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(top = 210.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            if (sortedHistory.size == 1) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = dateFormat.format(Date(sortedHistory[0].date)),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                sortedHistory.forEach { entry ->
                    Text(
                        text = dateFormat.format(Date(entry.date)),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
} 