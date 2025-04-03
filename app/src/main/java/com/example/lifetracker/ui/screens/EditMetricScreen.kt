package com.example.lifetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.  DateRange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.components.DatePickerDialog
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.formatDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMetricScreen(
    title: String,
    metricName: String,
    unit: String,
    navController: NavController,
    viewModel: HealthViewModel,
    onSave: (String, Long) -> Unit
) {
    val metrics = viewModel.metrics

    val initialValue = when (metricName) {
        "Weight" -> metrics.weight.toString()
        "Height" -> metrics.height.toString()
        "Body Fat" -> metrics.bodyFat.toString()
        else -> ""
    }

    var textValue by remember { mutableStateOf(initialValue) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    val history = remember { mutableStateOf(viewModel.getMetricHistory(metricName, unit)) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Input field
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Enter value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Date selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${formatDate(selectedDate)}",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = Color.White
                    )
                }
            }

            if (unit.isNotEmpty()) {
                Text(
                    text = "Unit: $unit",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Save button
            Button(
                onClick = {
                    onSave(textValue, selectedDate)
                    history.value = viewModel.getMetricHistory(metricName, unit)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Text("Save", fontSize = 16.sp)
            }

            // History section header with Edit/Done button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isEditMode) "Done" else "Edit",
                    color = Color.Blue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { isEditMode = !isEditMode }
                )
            }

            // History items
            LazyColumn {
                items(history.value) { entry ->
                    HistoryItem(
                        entry = entry,
                        unit = unit,
                        isEditMode = isEditMode,
                        onDelete = {
                            viewModel.deleteHistoryEntry(metricName, entry)
                            history.value = viewModel.getMetricHistory(metricName, unit)
                        }
                    )
                    Divider(color = Color(0xFF333333), thickness = 1.dp)
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun HistoryItem(
    entry: HistoryEntry,
    unit: String,
    isEditMode: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete button (only visible in edit mode)
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Value with unit
        Text(
            text = "${entry.value} $unit",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Date
        Text(
            text = formatDate(entry.date, includeTime = true),
            color = Color.Gray,
            fontSize = 14.sp
        )

        // Arrow (only visible when not in edit mode)
        if (!isEditMode) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}


// Helper function to format date with optional time
fun formatDate(timestamp: Long, includeTime: Boolean = false): String {
    val pattern = if (includeTime) "MMM d 'at' H:mm" else "MMM dd, yyyy"
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(timestamp))
}
