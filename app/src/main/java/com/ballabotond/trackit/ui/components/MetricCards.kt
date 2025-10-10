package com.ballabotond.trackit.ui.components

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
import com.ballabotond.trackit.ui.common.AppIcon

/**
 * A redesigned metric card that displays a title, icon, and a large value with its unit.
 * This Composable adapts its layout to show either the formatted metric or a "No Data" state.
 */
@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    iconName: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        // The new card is taller and more square-like
        modifier = modifier
            .aspectRatio(1.7f) // Makes the card a perfect square
            .heightIn(min = 120.dp),
        shape = RoundedCornerShape(24.dp), // More pronounced rounded corners
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)) // Slightly different dark color
    ) {
        // The main layout is now a Column, arranged with space between top and bottom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // Pushes the content to top and bottom
        ) {
            // Top Section: Icon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    iconName = iconName,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp),
                    contentDescription = "$title icon",
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom Section: Value and Unit, or "No Data" text
            // This conditional logic handles both states shown in your images.
            if (value == "No Data") {
                Text(
                    text = "No Data",
                    color = Color.Gray,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom // Aligns the unit with the bottom of the large value
                ) {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 38.sp, // Much larger font size for the value
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 3.dp) // Fine-tune baseline alignment
                    )
                }
            }
        }
    }
}