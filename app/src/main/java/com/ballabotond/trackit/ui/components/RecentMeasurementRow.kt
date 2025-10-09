package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.data.model.HistoryEntry
// --- CHANGE 1: Import our new AppIcon ---
import com.ballabotond.trackit.ui.common.AppIcon
import com.ballabotond.trackit.ui.theme.IconChoose
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecentMeasurementRow(entry: HistoryEntry, onClick: () -> Unit) {
    // --- CHANGE 2: Use getVisuals() and destructure into clear variable names ---
    val (iconName, iconColor) = IconChoose.getVisuals(entry.metricName)
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
                // Use the new iconColor variable
                .background(iconColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // --- CHANGE 3: Replace FontAwesomeIcon with AppIcon ---
            AppIcon(
                iconName = iconName,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
                contentDescription = "${entry.metricName} icon"
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