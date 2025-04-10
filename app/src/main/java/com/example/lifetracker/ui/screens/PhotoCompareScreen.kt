package com.example.lifetracker.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.data.model.PhotoCategory
import com.example.lifetracker.data.model.PhotoMetadata
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.ui.viewmodel.PhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCompareScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    mainPhotoUri: String,
    comparePhotoUri: String
) {
    val context = LocalContext.current
    val photoViewModel = remember { PhotoViewModel() }
    
    // Load photos to get categories
    LaunchedEffect(Unit) {
        photoViewModel.loadPhotos(context)
    }
    
    // Decode the URL-encoded paths
    val decodedMainPath = try {
        java.net.URLDecoder.decode(mainPhotoUri, "UTF-8")
    } catch (e: Exception) {
        mainPhotoUri
    }
    
    val decodedComparePath = try {
        java.net.URLDecoder.decode(comparePhotoUri, "UTF-8")
    } catch (e: Exception) {
        comparePhotoUri
    }
    
    val mainFile = File(decodedMainPath)
    val compareFile = File(decodedComparePath)
    
    val mainUri = Uri.fromFile(mainFile)
    val compareUri = Uri.fromFile(compareFile)
    
    // Get photo info
    val mainPhoto = photoViewModel.photos.find { it.filePath == decodedMainPath }
    val comparePhoto = photoViewModel.photos.find { it.filePath == decodedComparePath }
    
    val mainCategory = mainPhoto?.category ?: PhotoCategory.OTHER
    val compareCategory = comparePhoto?.category ?: PhotoCategory.OTHER
    
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

    val mainMetadata = mainPhoto?.metadata ?: PhotoMetadata()
    val compareMetadata = comparePhoto?.metadata ?: PhotoMetadata()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compare ${mainCategory.displayName}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Photo comparison
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Dates row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mainDate,
                        color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = compareDate,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Images row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(Color(0xFF111111))
                        ) {
                    AsyncImage(
                        model = mainUri,
                        contentDescription = "Main Photo",
                        modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(Color(0xFF111111))
                        ) {
                    AsyncImage(
                        model = compareUri,
                        contentDescription = "Compare Photo",
                        modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            
            // Metadata comparison
            if (mainMetadata.weight != null || compareMetadata.weight != null ||
                mainMetadata.bodyFatPercentage != null || compareMetadata.bodyFatPercentage != null ||
                mainMetadata.measurements.isNotEmpty() || compareMetadata.measurements.isNotEmpty()) {
                
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "METRICS COMPARISON",
                            color = Color(0xFF2196F3),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        // Weight comparison
                        if (mainMetadata.weight != null || compareMetadata.weight != null) {
                            ComparisonRow(
                                title = "Weight",
                                valueLeft = mainMetadata.weight?.toString() ?: "-",
                                valueRight = compareMetadata.weight?.toString() ?: "-",
                                unit = "kg",
                                showDifference = mainMetadata.weight != null && compareMetadata.weight != null
                            )
                        }
                        
                        // Body fat comparison
                        if (mainMetadata.bodyFatPercentage != null || compareMetadata.bodyFatPercentage != null) {
                            ComparisonRow(
                                title = "Body Fat",
                                valueLeft = mainMetadata.bodyFatPercentage?.toString() ?: "-",
                                valueRight = compareMetadata.bodyFatPercentage?.toString() ?: "-",
                                unit = "%",
                                showDifference = mainMetadata.bodyFatPercentage != null && compareMetadata.bodyFatPercentage != null
                            )
                        }
                        
                        // Measurements comparison
                        val allMeasurementKeys = (mainMetadata.measurements.keys + compareMetadata.measurements.keys).toSet()
                        allMeasurementKeys.forEach { key ->
                            val mainValue = mainMetadata.measurements[key]
                            val compareValue = compareMetadata.measurements[key]
                            
                            if (mainValue != null || compareValue != null) {
                                ComparisonRow(
                                    title = key.replaceFirstChar { it.uppercase() },
                                    valueLeft = mainValue?.toString() ?: "-",
                                    valueRight = compareValue?.toString() ?: "-",
                                    unit = "cm",
                                    showDifference = mainValue != null && compareValue != null
                                )
                            }
                        }
                        
                        // Notes if any
                        if (mainMetadata.notes.isNotEmpty() || compareMetadata.notes.isNotEmpty()) {
                            Divider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color(0xFF333333)
                            )
                            
                            if (mainMetadata.notes.isNotEmpty()) {
                                Text(
                                    text = "NOTES (${mainDate})",
                                    color = Color(0xFFFFB600),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = mainMetadata.notes,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                            
                            if (compareMetadata.notes.isNotEmpty()) {
                                Text(
                                    text = "NOTES (${compareDate})",
                                    color = Color(0xFFFFB600),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = compareMetadata.notes,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Tips card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tips",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "COMPARISON TIPS",
                            color = Color(0xFF2196F3),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFF333333)
                    )
                    
                    BulletPoint(text = "Look for changes in definition and muscle tone")
                    BulletPoint(text = "Compare symmetry and proportions")
                    BulletPoint(text = "Track visible progress in specific areas")
                    BulletPoint(text = "For best results, compare photos with similar lighting and poses")
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = Color(0xFF2196F3),
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ComparisonRow(
    title: String,
    valueLeft: String,
    valueRight: String,
    unit: String,
    showDifference: Boolean = false
) {
    // Calculate the difference outside the composable
    val differenceData = if (showDifference && valueLeft != "-" && valueRight != "-") {
        calculateDifference(valueLeft, valueRight)
    } else null
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First value
        Text(
                text = "$valueLeft $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            // Difference display
            differenceData?.let { (diffText, diffColor, sign) ->
        Text(
                    text = "$sign$diffText $unit",
                    color = diffColor,
            fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
        )
            }
        
            // Second value
        Text(
                text = "$valueRight $unit",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFF333333)
        )
    }
}

// Helper function to calculate difference
private fun calculateDifference(valueLeft: String, valueRight: String): Triple<String, Color, String>? {
    return try {
        val leftVal = valueLeft.toFloat()
        val rightVal = valueRight.toFloat()
        val diff = rightVal - leftVal
        val diffText = String.format("%.1f", diff)
        val diffColor = when {
            diff > 0 -> Color(0xFF4CAF50) // Green for increase
            diff < 0 -> Color(0xFFF44336) // Red for decrease
            else -> Color.LightGray      // Gray for no change
        }
        
        val sign = if (diff > 0) "+" else ""
        Triple(diffText, diffColor, sign)
    } catch (e: Exception) {
        null
    }
} 