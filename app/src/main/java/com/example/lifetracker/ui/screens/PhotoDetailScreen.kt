package com.example.lifetracker.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.ui.viewmodel.PhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotoDetailScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    photoUri: String
) {
    val context = LocalContext.current
    val photoViewModel = remember { PhotoViewModel() }
    
    // Decode the URL-encoded path
    val decodedPath = try {
        java.net.URLDecoder.decode(photoUri, "UTF-8")
    } catch (e: Exception) {
        photoUri
    }
    
    val file = File(decodedPath)
    val uri = Uri.fromFile(file)
    
    val date = try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(file.lastModified()))
    } catch (e: Exception) {
        "Unknown date"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar with back button and delete button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                    text = date,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        photoViewModel.deletePhoto(context, uri)
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }

            // Photo display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Measurements section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Body Measurements Section
                Text(
                    text = "BODY MEASUREMENTS",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1A1A1A),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Regular body measurements
                        MeasurementCard("Weight", viewModel.getLatestHistoryEntry("Weight", "kg")?.toString() ?: "No data", "kg")
                        MeasurementCard("Height", viewModel.getLatestHistoryEntry("Height", "cm")?.toString() ?: "No data", "cm")
                        MeasurementCard("Body Fat", viewModel.getLatestHistoryEntry("Body Fat", "%")?.toString() ?: "No data", "%")
                        MeasurementCard("Waist", viewModel.getLatestHistoryEntry("Waist", "cm")?.toString() ?: "No data", "cm")
                        MeasurementCard("Bicep", viewModel.getLatestHistoryEntry("Bicep", "cm")?.toString() ?: "No data", "cm")
                        MeasurementCard("Chest", viewModel.getLatestHistoryEntry("Chest", "cm")?.toString() ?: "No data", "cm")
                        MeasurementCard("Thigh", viewModel.getLatestHistoryEntry("Thigh", "cm")?.toString() ?: "No data", "cm")
                        MeasurementCard("Shoulder", viewModel.getLatestHistoryEntry("Shoulder", "cm")?.toString() ?: "No data", "cm")
                    }
                }
                
                // Calculated Measurements Section
                Text(
                    text = "CALCULATED",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1A1A1A),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Calculate BMI
                        val weight = viewModel.getLatestHistoryEntry("Weight", "kg") ?: 0f
                        val height = viewModel.getLatestHistoryEntry("Height", "cm") ?: 0f
                        val bmi = if (weight > 0 && height > 0) {
                            String.format("%.1f", weight / ((height / 100) * (height / 100)))
                        } else "No data"
                        
                        // Calculated metrics
                        MeasurementCard("BMI", bmi, "")
                        MeasurementCard("Lean Body Mass", "58.2", "kg")
                        MeasurementCard("Fat Mass", "10.3", "kg")
                        MeasurementCard("Fat-Free Mass Index", "19.0", "")
                        MeasurementCard("Basal Metabolic Rate", "1628", "kcal")
                        MeasurementCard("Body Surface Area", "1.8", "m²")
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(
    title: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = if (unit.isEmpty()) value else "$value $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Divider(color = Color(0xFF333333), thickness = 0.5.dp)
} 