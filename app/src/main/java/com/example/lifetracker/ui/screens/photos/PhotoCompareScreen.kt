package com.example.lifetracker.ui.screens.photos

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.LayoutDirection
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
import android.util.Log

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
    val decodedMainPath = java.net.URLDecoder.decode(mainPhotoUri, "UTF-8")
    val decodedComparePath = java.net.URLDecoder.decode(comparePhotoUri, "UTF-8")
    
    val mainFile = File(decodedMainPath)
    val compareFile = File(decodedComparePath)
    
    val mainUri = Uri.fromFile(mainFile)
    val compareUri = Uri.fromFile(compareFile)
    
    // Get photo info
    val mainPhoto = photoViewModel.photos.find { it.filePath == decodedMainPath }
    val comparePhoto = photoViewModel.photos.find { it.filePath == decodedComparePath }
    
    val mainCategory = mainPhoto?.category ?: PhotoCategory.OTHER
    val compareCategory = comparePhoto?.category ?: PhotoCategory.OTHER
    
    val mainDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        .format(Date(mainFile.lastModified()))
    val compareDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        .format(Date(compareFile.lastModified()))

    val mainMetadata = mainPhoto?.metadata ?: PhotoMetadata()
    val compareMetadata = comparePhoto?.metadata ?: PhotoMetadata()
    
    // Get metrics for both photos
    val getPhotoMetrics = { photoFile: File ->
        val photoDate = photoFile.lastModified()
        val metrics = listOf(
            "Weight" to "kg",
            "Height" to "cm",
            "Body Fat" to "%",
            "Waist" to "cm",
            "Bicep" to "cm",
            "Chest" to "cm",
            "Thigh" to "cm",
            "Shoulder" to "cm"
        )
        
        metrics.mapNotNull { (metric, unit) ->
            val history = viewModel.getMetricHistory(metric, unit)
            if (history.isEmpty()) return@mapNotNull null
            
            // Find entries recorded before or on the photo date
            val entriesBeforePhoto = history.filter { it.date <= photoDate }
            
            if (entriesBeforePhoto.isNotEmpty()) {
                // Get the latest entry before or on the photo date
                val latestEntry = entriesBeforePhoto.maxByOrNull { it.date }
                if (latestEntry != null) {
                    Log.d("PhotoMetrics", "Found metric $metric with value ${latestEntry.value} for photo date ${Date(photoDate)}")
                    metric to (latestEntry to unit)
                } else null
            } else {
                Log.d("PhotoMetrics", "No entries before photo date ${Date(photoDate)} for metric $metric")
                null
            }
        }
    }
    
    val mainMetricEntries = getPhotoMetrics(mainFile)
    val compareMetricEntries = getPhotoMetrics(compareFile)
    
    // Log metric entries for debugging
    Log.d("PhotoMetrics", "Main photo (${mainDate}): Found ${mainMetricEntries.size} metrics")
    mainMetricEntries.forEach { (metric, data) ->
        val (entry, unit) = data
        Log.d("PhotoMetrics", "  $metric: ${entry.value} $unit (recorded on ${Date(entry.date)})")
    }
    
    Log.d("PhotoMetrics", "Compare photo (${compareDate}): Found ${compareMetricEntries.size} metrics")
    compareMetricEntries.forEach { (metric, data) ->
        val (entry, unit) = data
        Log.d("PhotoMetrics", "  $metric: ${entry.value} $unit (recorded on ${Date(entry.date)})")
    }
    
    // Format metric value for display
    val formatMetricValue = { metric: String, value: Float, unit: String ->
        if (unit == "cm" && metric != "Height") {
            String.format("%.1f %s", value, unit)
        } else if (metric == "Height") {
            String.format("%d %s", value.toInt(), unit)
        } else {
            String.format("%.1f%s", value, unit)
        }
    }

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
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Tip card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "TIPS FOR COMPARISON",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Look for changes in body composition, muscle definition, and overall shape rather than just weight.",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Photos comparison
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Main photo
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                    ) {
                        AsyncImage(
                            model = mainUri,
                            contentDescription = "Main Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Metrics overlay for main photo
                        if (mainMetricEntries.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x88000000),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .fillMaxWidth(0.9f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    // Basic metrics (Weight, Height, Body Fat)
                                    val basicMetrics = mainMetricEntries.filter { (metric, _) ->
                                        metric in listOf("Weight", "Body Fat")
                                    }
                                    basicMetrics.forEach { (metric, entry) ->
                                        val (value, unit) = entry
                                        Text(
                                            text = "$metric: ${formatMetricValue(metric, value.value, unit)}",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = mainDate,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Compare photo
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                    ) {
                        AsyncImage(
                            model = compareUri,
                            contentDescription = "Compare Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Metrics overlay for compare photo
                        if (compareMetricEntries.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x88000000),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .fillMaxWidth(0.9f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    // Basic metrics (Weight, Height, Body Fat)
                                    val basicMetrics = compareMetricEntries.filter { (metric, _) ->
                                        metric in listOf("Weight", "Body Fat")
                                    }
                                    basicMetrics.forEach { (metric, entry) ->
                                        val (value, unit) = entry
                                        Text(
                                            text = "$metric: ${formatMetricValue(metric, value.value, unit)}",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = compareDate,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Metrics Comparison
            if (mainMetricEntries.isNotEmpty() || compareMetricEntries.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Combine metrics from both photos
                        val allMetrics = (mainMetricEntries.map { it.first } + compareMetricEntries.map { it.first }).distinct()
                        
                        allMetrics.forEach { metricName ->
                            val mainEntry = mainMetricEntries.find { it.first == metricName }
                            val compareEntry = compareMetricEntries.find { it.first == metricName }
                            
                            if (mainEntry != null || compareEntry != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = metricName,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        modifier = Modifier.width(100.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // Old value (main photo)
                                    Text(
                                        text = mainEntry?.let { 
                                            val (value, unit) = it.second
                                            formatMetricValue(metricName, value.value, unit)
                                        } ?: "-",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(90.dp)
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "to",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    // New value (compare photo)
                                    Text(
                                        text = compareEntry?.let { 
                                            val (value, unit) = it.second
                                            formatMetricValue(metricName, value.value, unit)
                                        } ?: "-",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(90.dp)
                                    )
                                }
                                Divider(color = Color(0xFF333333), thickness = 0.5.dp)
                            }
                        }
                    }
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