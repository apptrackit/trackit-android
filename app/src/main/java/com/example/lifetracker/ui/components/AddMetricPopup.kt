package com.example.lifetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.lifetracker.ui.theme.FontAwesomeIcon
import com.guru.fontawesomecomposelib.FaIcons

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

                
                MetricOption(
                    title = "Weight",
                    icon = FaIcons.Weight,
                    iconTint = Color(0xFF2196F3),
                    onClick = { 
                        onNavigateToEditMetric("Weight")
                        onDismiss()
                    }
                )
                
                MetricOption(
                    title = "Height",
                    icon = FaIcons.RulerVertical,
                    iconTint = Color(0xFF9C27B0),
                    onClick = { 
                        onNavigateToEditMetric("Height")
                        onDismiss()
                    }
                )
                
                MetricOption(
                    title = "Body Fat",
                    icon = FaIcons.Percent,
                    iconTint = Color(0xFF4CAF50),
                    onClick = { 
                        onNavigateToEditMetric("Body Fat")
                        onDismiss()
                    }
                )
                
                MetricOption(
                    title = "Chest",
                    icon = FaIcons.Male,
                    iconTint = Color(0xFFFF9800),
                    onClick = { 
                        onNavigateToEditMetric("Chest")
                        onDismiss()
                    }
                )
                
                MetricOption(
                    title = "Waist",
                    icon = FaIcons.Tape,
                    iconTint = Color(0xFF00BCD4),
                    onClick = { 
                        onNavigateToEditMetric("Waist")
                        onDismiss()
                    }
                )
                
                MetricOption(
                    title = "Bicep",
                    icon = FaIcons.Dumbbell,
                    iconTint = Color(0xFFFF5722),
                    onClick = { 
                        onNavigateToEditMetric("Bicep")
                        onDismiss()
                    }
                )
                
                MetricOption(
                    title = "Thigh",
                    icon = FaIcons.Child,
                    iconTint = Color(0xFF00BCD4),
                    onClick = { 
                        onNavigateToEditMetric("Thigh")
                        onDismiss()
                    }
                )
                
                MetricOption(
                    title = "Shoulder",
                    icon = FaIcons.Male,
                    iconTint = Color(0xFFFF9800),
                    onClick = { 
                        onNavigateToEditMetric("Shoulder")
                        onDismiss()
                    }
                )
                
                // Removed Custom Measurement section
            }
        }
    }
}

@Composable
private fun MetricOption(
    title: String,
    icon: com.guru.fontawesomecomposelib.FaIconType.SolidIcon,
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
            FontAwesomeIcon(
                icon = icon,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
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
