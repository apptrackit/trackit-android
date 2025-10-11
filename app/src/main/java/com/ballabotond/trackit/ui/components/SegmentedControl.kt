package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A custom segmented control for time filtering that matches the new design.
 *
 * @param items A list of strings to display as options in the control.
 * @param selectedItem The currently selected item from the list.
 * @param onItemClick A callback that is invoked with the new item when one is clicked.
 */
@Composable
fun SegmentedControl(
    items: List<String>,
    selectedItem: String,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .height(38.dp) // Set the overall height of the control
            .clip(RoundedCornerShape(30.dp)) // This creates the outer pill shape
            .background(Color(0xFF1C1C1E)),   // The dark gray background of the whole bar
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem

            Box(
                modifier = Modifier
                    .weight(1f) // Each item will take up an equal amount of space
                    .fillMaxHeight()
                    // The inner selection pill shape
                    .padding(3.dp) // Adds a small gap between the selection and the outer edge
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        // The background is a lighter gray only when selected
                        if (isSelected) Color.DarkGray else Color.Transparent
                    )
                    .clickable { onItemClick(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}