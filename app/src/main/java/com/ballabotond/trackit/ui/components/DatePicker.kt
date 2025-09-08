package com.ballabotond.trackit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Select Date",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Date picker with custom styling
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(bottom = 24.dp),
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color(0xFF8E8E93),
                        subheadContentColor = Color.White,
                        yearContentColor = Color.White,
                        currentYearContentColor = Color(0xFF007AFF),
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = Color(0xFF007AFF),
                        dayContentColor = Color.White,
                        disabledDayContentColor = Color(0xFF3A3A3C),
                        selectedDayContentColor = Color.White,
                        disabledSelectedDayContentColor = Color(0xFF8E8E93),
                        selectedDayContainerColor = Color(0xFF007AFF),
                        disabledSelectedDayContainerColor = Color(0xFF3A3A3C),
                        todayContentColor = Color(0xFF007AFF),
                        todayDateBorderColor = Color(0xFF007AFF),
                        dayInSelectionRangeContainerColor = Color(0xFF007AFF).copy(alpha = 0.3f),
                        dayInSelectionRangeContentColor = Color.White
                    )
                )
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF8E8E93))
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF007AFF))
                    ) {
                        Text(
                            "Confirm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
