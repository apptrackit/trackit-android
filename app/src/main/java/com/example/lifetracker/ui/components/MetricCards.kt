package com.example.lifetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        MetricCardContent(title, value, unit)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        MetricCardContent(title, value, unit)
    }
}

@Composable
fun MetricCardContent(title: String, value: String, unit: String) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .height(80.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp
        )
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
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }
    }
}
