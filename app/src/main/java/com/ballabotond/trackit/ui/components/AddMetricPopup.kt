package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.ballabotond.trackit.ui.theme.FeatherIcon
import com.ballabotond.trackit.ui.theme.IconChoose
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AddMetricPopup(
    onDismiss: () -> Unit,
    onNavigateToEditMetric: (String) -> Unit,
    navController: NavController
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Add Measurement",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                Divider(color = Color(0xFF333333), thickness = 1.dp)

                val metrics = listOf(
                    "Weight",
                    "Height",
                    "Body Fat",
                    "Chest",
                    "Waist",
                    "Bicep",
                    "Thigh",
                    "Shoulder"
                )
                metrics.forEach { metric ->
                    val (icon, iconTint) = IconChoose.getIcon(metric)
                    MetricOption(
                        title = metric,
                        icon = icon,
                        iconTint = iconTint,
                        onClick = { 
                            onNavigateToEditMetric(metric)
                            onDismiss()
                        }
                    )
                }
                
                // Removed Custom Measurement section
            }
        }
    }
}

@Composable
private fun MetricOption(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with circular background
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            FeatherIcon(
                icon = icon,
                tint = iconTint,
                size = 20.dp
            )
        }
        
        // Title
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
