package com.example.lifetracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.formatDate
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditMetricScreen(
    title: String,
    metricName: String,
    unit: String,
    navController: NavController,
    viewModel: HealthViewModel,
    onSave: (String, Long) -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }

    val history = remember { mutableStateOf(viewModel.getMetricHistory(metricName, unit)) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button, title, and plus button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button and title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Plus button with improved styling
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                        .clickable {
                            navController.navigate("add_metric_data/$metricName/$unit/$title")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Entry",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Graph
            MetricHistoryChart(history = history.value, unit = unit)

            // History section header with Edit/Done button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Edit/Done button
                Text(
                    text = if (isEditMode) "Done" else "Edit",
                    color = Color.Blue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { isEditMode = !isEditMode }
                )
            }

            // History items
            LazyColumn {
                items(history.value) { entry ->
                    HistoryItem(
                        entry = entry,
                        unit = unit,
                        isEditMode = isEditMode,
                        onDelete = {
                            viewModel.deleteHistoryEntry(metricName, entry)
                            history.value = viewModel.getMetricHistory(metricName, unit)
                        }
                    )
                    Divider(color = Color(0xFF333333), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun MetricHistoryChart(history: List<HistoryEntry>, unit: String) {
    if (history.isEmpty()) return

    val sortedHistory = history.sortedBy { it.date }
    val minValue = sortedHistory.minOf { it.value }
    val maxValue = sortedHistory.maxOf { it.value }
    val valueRange = maxValue - minValue

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val xStep = width / (sortedHistory.size - 1)

        // Draw axis
        drawLine(Color.White, Offset(0f, height), Offset(width, height))
        drawLine(Color.White, Offset(0f, 0f), Offset(0f, height))

        // Draw data points and lines
        val path = Path()
        sortedHistory.forEachIndexed { index, entry ->
            val x = index * xStep
            val y = height - (entry.value - minValue) / valueRange * height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            drawCircle(Color.Blue, 4.dp.toPx(), Offset(x, y))
        }

        drawPath(path, Color.Blue, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))

        // Draw labels
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        sortedHistory.forEachIndexed { index, entry ->
            val x = index * xStep
            drawContext.canvas.nativeCanvas.drawText(
                dateFormat.format(Date(entry.date)),
                x,
                height + 20,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun HistoryItem(
    entry: HistoryEntry,
    unit: String,
    isEditMode: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete button (only visible in edit mode)
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Red, CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Value with unit
        Text(
            text = "${entry.value} $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Date
        Text(
            text = formatDate(entry.date),
            color = Color.Gray,
            fontSize = 14.sp
        )

        // Arrow (only visible when in edit mode)
        if (isEditMode) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
