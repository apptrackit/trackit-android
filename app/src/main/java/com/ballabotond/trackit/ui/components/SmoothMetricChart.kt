package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.data.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class) // This annotation is required for the new text drawing API
@Composable
fun SmoothMetricChart(history: List<HistoryEntry>, unit: String, modifier: Modifier = Modifier) {
    // If there isn't enough data to draw a line, show a placeholder.
    if (history.size < 2) {
        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Not enough data for chart.", color = Color(0xFF444444), fontSize = 12.sp)
        }
        return
    }

    val sortedHistory = history.sortedBy { it.date }
    val minValue = sortedHistory.minOf { it.value }
    val maxValue = sortedHistory.maxOf { it.value }

    val range = (maxValue - minValue).coerceAtLeast(0.1f)
    val paddedMin = (minValue - range * 0.1f).coerceAtLeast(0f)
    val paddedMax = maxValue + range * 0.1f
    val valueRange = paddedMax - paddedMin

    val textMeasurer = rememberTextMeasurer() // The modern, safe way to draw text

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 45.dp, end = 10.dp, top = 10.dp, bottom = 25.dp)
        ) {
            val width = size.width
            val height = size.height

            // --- Y-AXIS LABELS AND GRID ---
            val ySteps = 4
            for (i in 0..ySteps) {
                val y = height - (i.toFloat() / ySteps * height)
                val value = paddedMin + (i.toFloat() / ySteps * valueRange)

                // Draw the horizontal grid line
                drawLine(
                    color = Color(0xFF333333),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )

                // Prepare the text to be drawn
                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(String.format("%.1f", value).removeSuffix(".0")),
                    style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                )

                // Draw the text using the modern API
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = -textLayoutResult.size.width - 8.dp.toPx(), // Position left of the canvas
                        y = y - textLayoutResult.size.height / 2      // Center vertically on the line
                    )
                )
            }

            // --- YOUR ORIGINAL PATH DRAWING LOGIC (UNCHANGED) ---
            val path = Path()
            val points = sortedHistory.mapIndexed { index, entry ->
                val x = if (sortedHistory.size > 1) index * width / (sortedHistory.size - 1) else 0f
                val normalizedValue = if (valueRange > 0) (entry.value - paddedMin) / valueRange else 0.5f
                val y = height - normalizedValue * height
                Offset(x, y)
            }
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val current = points[i]
                val controlX1 = prev.x + (current.x - prev.x) / 3
                val controlY1 = prev.y
                val controlX2 = current.x - (current.x - prev.x) / 3
                val controlY2 = current.y
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, current.x, current.y)
            }
            drawPath(
                path = path,
                color = Color(0xFF2196F3),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // --- CORRECTED X-AXIS LABELS ---
            if (sortedHistory.size > 1) {
                val dateFormat = SimpleDateFormat("MMM", Locale.getDefault()) // Use "MMM" for month
                val labelCount = 4

                for (i in 0..labelCount) {
                    val index = (i * (sortedHistory.size - 1) / (labelCount - 1).toFloat()).roundToInt().coerceIn(0, sortedHistory.lastIndex)
                    val x = index * width / (sortedHistory.size - 1)
                    val date = Date(sortedHistory[index].date)

                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(dateFormat.format(date)),
                        style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = x - textLayoutResult.size.width / 2, // Center horizontally
                            y = height + 8.dp.toPx()                 // Position below the chart
                        )
                    )
                }
            }
        }
    }
}