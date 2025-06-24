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
import com.ballabotond.trackit.ui.theme.FontAwesomeIcon
import com.ballabotond.trackit.ui.theme.IconChoose
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecentMeasurementRow(entry: HistoryEntry, onClick: () -> Unit) {
    val iconAndColor = IconChoose.getIcon(entry.metricName)
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
                .background(iconAndColor.second.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            FontAwesomeIcon(
                icon = iconAndColor.first,
                tint = iconAndColor.second,
                modifier = Modifier
                    .size(20.dp)
                    .padding(0.dp)
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
