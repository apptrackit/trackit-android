package com.example.lifetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import com.example.lifetracker.ui.components.DatePickerDialog
import com.example.lifetracker.ui.components.HistoryItem
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.utils.formatDate

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
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                    // Refresh history after saving
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

            // History section
            Text(
                text = "History",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            // Display history entries
            LazyColumn {
                items(history.value) { entry ->
                    HistoryItem(
                        date = entry.date,
                        value = entry.value,
                        unit = unit
                    )
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
