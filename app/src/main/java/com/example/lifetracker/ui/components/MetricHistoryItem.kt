package com.example.lifetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.utils.formatDate

@Composable
fun MetricHistoryItem(entry: HistoryEntry, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Metric value
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Value
            Text(
                text = String.format("%.1f", entry.value).let { 
                    if (it.endsWith(".0")) it.substring(0, it.length - 2) else it 
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Unit
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    color = Color(0xFF888888),
                    fontSize = 10.sp
                )
            }
        }

        // Date
        Text(
            text = formatDate(entry.date),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
} 