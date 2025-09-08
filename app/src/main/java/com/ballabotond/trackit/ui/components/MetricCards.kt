package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.ballabotond.trackit.ui.theme.FeatherIcon
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MetricCardRedesignedWithFeatherIcon(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FeatherIcon(
                    icon = icon,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                // Different display style for "No Data" versus actual measurements
                if (value == "No Data") {
                    Text(
                        text = value,
                        color = Color(0xFF666666), // Darker gray for "No Data"
                        fontSize = 16.sp, // Smaller font size
                        fontWeight = FontWeight.Normal // Not bold
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unit.isNotEmpty()) {
                            Text(
                                text = unit,
                                color = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 3.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
