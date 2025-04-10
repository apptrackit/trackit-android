package com.example.lifetracker.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotoCompareScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    mainPhotoUri: String,
    comparePhotoUri: String
) {
    // Decode the URL-encoded paths
    val mainDecodedPath = try {
        java.net.URLDecoder.decode(mainPhotoUri, "UTF-8")
    } catch (e: Exception) {
        mainPhotoUri
    }
    
    val compareDecodedPath = try {
        java.net.URLDecoder.decode(comparePhotoUri, "UTF-8")
    } catch (e: Exception) {
        comparePhotoUri
    }
    
    val mainFile = File(mainDecodedPath)
    val compareFile = File(compareDecodedPath)
    
    val mainUri = Uri.fromFile(mainFile)
    val compareUri = Uri.fromFile(compareFile)
    
    val mainDate = try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(mainFile.lastModified()))
    } catch (e: Exception) {
        "Unknown date"
    }
    
    val compareDate = try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(compareFile.lastModified()))
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
            // Top bar with back button
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
                    text = "Photo Comparison",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // Empty box to balance the layout
                Box(modifier = Modifier.size(48.dp))
            }

            // Photos side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = mainDate,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    AsyncImage(
                        model = mainUri,
                        contentDescription = "Main Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = compareDate,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    AsyncImage(
                        model = compareUri,
                        contentDescription = "Compare Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // Measurements comparison
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "MEASUREMENTS COMPARISON",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1A1A1A),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Retrieve metrics for both dates
                        val mainWeight = viewModel.getHistoryEntryAtDate("Weight", "kg", mainFile.lastModified())?.toString() ?: "No data"
                        val compareWeight = viewModel.getHistoryEntryAtDate("Weight", "kg", compareFile.lastModified())?.toString() ?: "No data"
                        
                        val mainHeight = viewModel.getHistoryEntryAtDate("Height", "cm", mainFile.lastModified())?.toString() ?: "No data"
                        val compareHeight = viewModel.getHistoryEntryAtDate("Height", "cm", compareFile.lastModified())?.toString() ?: "No data"
                        
                        val mainBodyFat = viewModel.getHistoryEntryAtDate("Body Fat", "%", mainFile.lastModified())?.toString() ?: "No data"
                        val compareBodyFat = viewModel.getHistoryEntryAtDate("Body Fat", "%", compareFile.lastModified())?.toString() ?: "No data"
                        
                        val mainWaist = viewModel.getHistoryEntryAtDate("Waist", "cm", mainFile.lastModified())?.toString() ?: "No data"
                        val compareWaist = viewModel.getHistoryEntryAtDate("Waist", "cm", compareFile.lastModified())?.toString() ?: "No data"
                        
                        val mainBicep = viewModel.getHistoryEntryAtDate("Bicep", "cm", mainFile.lastModified())?.toString() ?: "No data"
                        val compareBicep = viewModel.getHistoryEntryAtDate("Bicep", "cm", compareFile.lastModified())?.toString() ?: "No data"
                        
                        val mainChest = viewModel.getHistoryEntryAtDate("Chest", "cm", mainFile.lastModified())?.toString() ?: "No data"
                        val compareChest = viewModel.getHistoryEntryAtDate("Chest", "cm", compareFile.lastModified())?.toString() ?: "No data"
                        
                        val mainThigh = viewModel.getHistoryEntryAtDate("Thigh", "cm", mainFile.lastModified())?.toString() ?: "No data"
                        val compareThigh = viewModel.getHistoryEntryAtDate("Thigh", "cm", compareFile.lastModified())?.toString() ?: "No data"
                        
                        val mainShoulder = viewModel.getHistoryEntryAtDate("Shoulder", "cm", mainFile.lastModified())?.toString() ?: "No data"
                        val compareShoulder = viewModel.getHistoryEntryAtDate("Shoulder", "cm", compareFile.lastModified())?.toString() ?: "No data"
                        
                        // Display metrics comparisons
                        ComparisonRow("Weight", mainWeight, compareWeight, "kg")
                        ComparisonRow("Height", mainHeight, compareHeight, "cm")
                        ComparisonRow("Body Fat", mainBodyFat, compareBodyFat, "%")
                        ComparisonRow("Waist", mainWaist, compareWaist, "cm")
                        ComparisonRow("Bicep", mainBicep, compareBicep, "cm")
                        ComparisonRow("Chest", mainChest, compareChest, "cm")
                        ComparisonRow("Thigh", mainThigh, compareThigh, "cm")
                        ComparisonRow("Shoulder", mainShoulder, compareShoulder, "cm")
                    }
                }
                
                // Calculated Metrics Comparison
                Text(
                    text = "CALCULATED COMPARISON",
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
                        // Calculate BMI for both dates
                        val mainWeight = viewModel.getHistoryEntryAtDate("Weight", "kg", mainFile.lastModified()) ?: 0f
                        val mainHeight = viewModel.getHistoryEntryAtDate("Height", "cm", mainFile.lastModified()) ?: 0f
                        val mainBmi = if (mainWeight > 0 && mainHeight > 0) {
                            String.format("%.1f", mainWeight / ((mainHeight / 100) * (mainHeight / 100)))
                        } else "No data"
                        
                        val compareWeight = viewModel.getHistoryEntryAtDate("Weight", "kg", compareFile.lastModified()) ?: 0f
                        val compareHeight = viewModel.getHistoryEntryAtDate("Height", "cm", compareFile.lastModified()) ?: 0f
                        val compareBmi = if (compareWeight > 0 && compareHeight > 0) {
                            String.format("%.1f", compareWeight / ((compareHeight / 100) * (compareHeight / 100)))
                        } else "No data"
                        
                        // Display calculated comparisons
                        ComparisonRow("BMI", mainBmi, compareBmi, "")
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    title: String,
    valueLeft: String,
    valueRight: String,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.width(100.dp)
        )
        
        // Left value
        Text(
            text = if (unit.isEmpty()) valueLeft else "$valueLeft $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(80.dp)
        )
        
        // Difference indicator
        val diff = try {
            if (valueLeft != "No data" && valueRight != "No data") {
                valueLeft.toFloat() - valueRight.toFloat()
            } else null
        } catch (e: Exception) {
            null
        }
        
        val diffText = diff?.let {
            if (it > 0) "+${String.format("%.1f", it)}" else String.format("%.1f", it)
        } ?: ""
        
        Text(
            text = diffText,
            color = when {
                diff == null -> Color.Gray
                diff > 0 -> Color(0xFF4CAF50) // Green for increase
                diff < 0 -> Color(0xFFE91E63) // Pink for decrease
                else -> Color.White
            },
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(60.dp)
        )
        
        // Right value
        Text(
            text = if (unit.isEmpty()) valueRight else "$valueRight $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(80.dp)
        )
    }
    Divider(color = Color(0xFF333333), thickness = 0.5.dp)
} 