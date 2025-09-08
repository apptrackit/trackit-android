package com.ballabotond.trackit.ui.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection
import com.ballabotond.trackit.ui.theme.FeatherIcon
import com.ballabotond.trackit.ui.theme.IconChoose
import com.ballabotond.trackit.ui.components.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditMetricDataScreen(
    title: String,
    metricName: String,
    unit: String,
    initialValue: Float,
    initialDate: Long,
    navController: NavController,
    viewModel: HealthViewModel,
    onSave: (String, Long) -> Unit
) {
    var inputValue by remember { mutableStateOf(initialValue.toString()) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialDate }) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section with close and check buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    FeatherIcon(
                        icon = FeatherIconsCollection.Close,
                        tint = Color.White,
                        size = 20.dp
                    )
                }
                
                // Check button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007AFF))
                        .clickable {
                            if (inputValue.isNotEmpty()) {
                                // Combine selected date with selected time
                                val dateCalendar = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate
                                    set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onSave(inputValue, dateCalendar.timeInMillis)
                                navController.popBackStack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    FeatherIcon(
                        icon = FeatherIconsCollection.Check,
                        tint = Color.White,
                        size = 20.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Large metric icon and name (fixed - no chooser)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val (icon, iconTint) = IconChoose.getIcon(metricName)
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    FeatherIcon(
                        icon = icon,
                        tint = iconTint,
                        size = 60.dp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = metricName,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Input fields section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Date field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(selectedDate)),
                        color = Color(0xFF007AFF),
                        fontSize = 18.sp
                    )
                }
                
                Divider(color = Color(0xFF333333), thickness = 1.dp)
                
                // Time field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time),
                        color = Color(0xFF007AFF),
                        fontSize = 18.sp
                    )
                }
                
                Divider(color = Color(0xFF333333), thickness = 1.dp)
                
                // Value input field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = unit,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        placeholder = { 
                            Text(
                                "0", 
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            ) 
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color(0xFF007AFF),
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF007AFF)
                        ),
                        modifier = Modifier.width(120.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.End,
                            fontSize = 18.sp
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { timestamp ->
                selectedDate = timestamp
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onTimeSelected = { calendar ->
                selectedTime = calendar
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: Calendar,
    onTimeSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Select Time",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Time picker with custom styling
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(bottom = 24.dp),
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFF2A2A2A),
                        selectorColor = Color(0xFF007AFF),
                        containerColor = Color.Transparent,
                        periodSelectorBorderColor = Color(0xFF007AFF),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color(0xFF8E8E93),
                        periodSelectorSelectedContainerColor = Color(0xFF007AFF),
                        periodSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = Color(0xFF8E8E93),
                        timeSelectorSelectedContainerColor = Color(0xFF007AFF),
                        timeSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = Color(0xFF8E8E93)
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
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                            }
                            onTimeSelected(calendar)
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
