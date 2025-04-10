package com.example.lifetracker.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.ui.navigation.PHOTO_COMPARE_ROUTE
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
    
    // For photo selection dialog
    var showPhotoSelectionDialog by remember { mutableStateOf(false) }
    
    // Load all photos for comparison dialog
    LaunchedEffect(Unit) {
        photoViewModel.loadPhotos(context)
    }
    
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

    // Photo selection dialog
    if (showPhotoSelectionDialog) {
        Dialog(onDismissRequest = { showPhotoSelectionDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A1A)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Photo to Compare",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showPhotoSelectionDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Grid of photos
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photoViewModel.photos.filter { it.path != uri.path }) { compareUri ->
                            val compareFile = File(compareUri.path ?: "")
                            val compareDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(compareFile.lastModified()))
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showPhotoSelectionDialog = false
                                        
                                        // Navigate to comparison screen
                                        val mainPath = uri.path ?: return@clickable
                                        val comparePath = compareUri.path ?: return@clickable
                                        
                                        val encodedMainPath = java.net.URLEncoder.encode(mainPath, "UTF-8")
                                        val encodedComparePath = java.net.URLEncoder.encode(comparePath, "UTF-8")
                                        
                                        navController.navigate(
                                            PHOTO_COMPARE_ROUTE
                                                .replace("{mainUri}", encodedMainPath)
                                                .replace("{compareUri}", encodedComparePath)
                                        )
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .fillMaxWidth()
                                ) {
                                    AsyncImage(
                                        model = compareUri,
                                        contentDescription = "Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = compareDate,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
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
                Row {
                    // Compare button
                    IconButton(onClick = { 
                        showPhotoSelectionDialog = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Compare",
                            tint = Color(0xFF2196F3)
                        )
                    }
                    // Delete button
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