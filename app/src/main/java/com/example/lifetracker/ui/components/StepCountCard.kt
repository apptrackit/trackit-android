package com.example.lifetracker.ui.components

import android.R.attr.textSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StepCountCard(
    steps: Int,
    isLoading: Boolean,
    weeklySteps: List<Pair<Long, Int>>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Step icon
                
                Text(
                    text = "Daily Steps",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Step count display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Loading steps...",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Text(
                        text = steps.toString(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " steps",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }
            }
            
            // Weekly step chart
            if (weeklySteps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = 16.dp)
                ) {
                    StepCountChart(steps = weeklySteps)
                }
            }
        }
    }
}

@Composable
private fun StepCountChart(steps: List<Pair<Long, Int>>) {
    val sortedSteps = steps.sortedBy { it.first }
    
    // Find max steps for scaling
    val maxSteps = sortedSteps.maxOfOrNull { it.second }?.toFloat() ?: 1f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barWidth = width / (sortedSteps.size * 2) // Leave space between bars
        val dateFormat = SimpleDateFormat("E", Locale.getDefault())
        
        // Draw bars
        sortedSteps.forEachIndexed { index, (date, count) ->
            val x = index * (width / sortedSteps.size) + (width / sortedSteps.size / 2) - barWidth / 2
            val barHeight = (count / maxSteps) * height * 0.8f
            val top = height - barHeight
            
            // Draw bar
            drawRect(
                color = Color(0xFF42A5F5),
                topLeft = Offset(x, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )

        }
    }
}
