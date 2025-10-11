package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ballabotond.trackit.data.model.HistoryEntry
import com.ballabotond.trackit.ui.common.AppIcon
import com.ballabotond.trackit.ui.theme.IconChoose
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecentMeasurementRow(entry: HistoryEntry, onClick: () -> Unit) {
    val (iconName, iconColor) = IconChoose.getVisuals(entry.metricName)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    // --- THIS IS THE CORRECTED FORMATTING LOGIC ---
    // It formats the number to one decimal place and removes ".0" if it's a whole number.
    val formattedValue = String.format("%.1f", entry.value).let {
        if (it.endsWith(".0")) it.substring(0, it.length - 2) else it
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Removed the old background and adjusted padding to match the new style
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // This Box creates the new circular background for the icon

        AppIcon(
            iconName = iconName, tint = iconColor, // The icon itself has the full color
            modifier = Modifier.size(24.dp), contentDescription = "${entry.metricName} icon"
        )


        Spacer(modifier = Modifier.width(16.dp))

        // Column for the metric name and date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.metricName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold // Title is now bold
            )
            Text(
                text = dateFormat.format(Date(entry.date)),
                color = Color.Gray, // Use a standard gray for subtitles
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // The formatted value and unit text on the right
        Text(
            text = "$formattedValue ${entry.unit}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium, // Use Medium weight for the value
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}