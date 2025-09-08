package com.ballabotond.trackit.ui.screens.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ballabotond.trackit.data.model.HistoryEntry
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.viewmodel.PhotoViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.ballabotond.trackit.ui.theme.IconChoose
import com.ballabotond.trackit.ui.theme.FeatherIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCompareScreen(
    navController: NavController,
    photoViewModel: PhotoViewModel,
    healthViewModel: HealthViewModel,
    mainPhotoId: String,
    comparePhotoId: String?
) {
    // Access photos directly from the PhotoViewModel
    val photos = photoViewModel.photos
    
    // Decode photo paths from the IDs (which are actually encoded file paths)
    val mainPhotoPath = try {
        java.net.URLDecoder.decode(mainPhotoId, "UTF-8")
    } catch (e: Exception) {
        return
    }
    
    val comparePhotoPath = comparePhotoId?.let { 
        try {
            java.net.URLDecoder.decode(it, "UTF-8")
        } catch (e: Exception) {
            null
        }
    }
    
    // Find photos by their file paths
    val mainPhoto = photos.find { it.filePath == mainPhotoPath }
    val comparePhoto = comparePhotoPath?.let { path -> 
        photos.find { it.filePath == path }
    }
    
    if (mainPhoto == null) {
        // Show error or navigate back
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // Get metric data for photos
    fun getPhotoMetrics(photoDate: Long): Map<String, Pair<HistoryEntry?, String>> {
        val metrics = listOf(
            "Weight" to "kg",
            "Body Fat" to "%",
            "Bicep" to "cm",
            "Chest" to "cm",
            "Waist" to "cm",
            "Thigh" to "cm",
            "Shoulder" to "cm"
        )
        
        return metrics.associate { (metric, unit) ->
            val history = healthViewModel.getMetricHistory(metric, unit)
            val entriesBeforePhoto = history.filter { it.date <= photoDate }
            val latestEntry = entriesBeforePhoto.maxByOrNull { it.date }
            metric to (latestEntry to unit)
        }
    }

    // Get metrics for both photos
    val mainMetrics = getPhotoMetrics(mainPhoto.timestamp.time)
    val compareMetrics = comparePhoto?.let { getPhotoMetrics(it.timestamp.time) } ?: emptyMap()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Progress Photos", 
                        color = Color.White, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add photo action */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add photo",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
        ) {
            // Category filter tabs - like in the mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryTab("All Phot...", "All", true, Color(0xFF007AFF))
                CategoryTab("Front", "Front", false, Color.Gray)
                CategoryTab("Side", "Side", false, Color.Gray)
                CategoryTab("Back", "Back", false, Color.Gray)
                CategoryTab("Arms", "Biceps", false, Color.Gray)
                CategoryTab("Chest", "Chest", false, Color.Gray)
            }
            
            // Comparison section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Comparison",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Photos side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // BEFORE photo
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Box {
                                AsyncImage(
                                    model = mainPhoto.uri,
                                    contentDescription = "Before photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.75f)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // BEFORE label
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(
                                            Color(0xFFFFCC02),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "BEFORE",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(mainPhoto.timestamp),
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // AFTER photo
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Box {
                                if (comparePhoto != null) {
                                    AsyncImage(
                                        model = comparePhoto.uri,
                                        contentDescription = "After photo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.75f)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.75f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF2C2C2E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No comparison photo",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                
                                // AFTER label
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(
                                            Color(0xFF2C2C2E),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "AFTER",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = comparePhoto?.let { 
                                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(it.timestamp)
                                } ?: "Select photo",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Metrics comparison
                    if (comparePhoto != null) {
                        val metricsToShow = listOf(
                            "Weight" to "kg",
                            "Body Fat" to "%", 
                            "Bicep" to "cm",
                            "Chest" to "cm",
                            "Waist" to "cm",
                            "Thigh" to "cm",
                            "Shoulder" to "cm"
                        )
                        
                        metricsToShow.forEach { (metricName, unit) ->
                            val beforeEntry = mainMetrics[metricName]?.first
                            val afterEntry = compareMetrics[metricName]?.first
                            
                            if (beforeEntry != null && afterEntry != null) {
                                MetricComparisonRow(
                                    metricName = metricName,
                                    beforeValue = beforeEntry.value.toDouble(),
                                    afterValue = afterEntry.value.toDouble(),
                                    unit = unit
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for navigation
        }
    }
}

@Composable
private fun CategoryTab(
    text: String,
    category: String,
    isSelected: Boolean,
    color: Color
) {
    val (icon, iconColor) = IconChoose.getIcon(category)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isSelected) color else Color(0xFF3A3A3C),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                FeatherIcon(
                    icon = icon,
                    tint = Color.White,
                    size = 18.dp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = text,
            color = if (isSelected) color else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MetricComparisonRow(
    metricName: String,
    beforeValue: Double,
    afterValue: Double,
    unit: String
) {
    val change = afterValue - beforeValue
    val changeColor = when {
        metricName == "Weight" || metricName == "Body Fat" || metricName == "Waist" -> {
            if (change < 0) Color(0xFF34C759) else Color(0xFFFF3B30) // Green for decrease, red for increase
        }
        else -> {
            if (change > 0) Color(0xFF34C759) else Color(0xFFFF3B30) // Green for increase, red for decrease
        }
    }
    
    val (icon, iconColor) = IconChoose.getIcon(metricName)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        FeatherIcon(
            icon = icon,
            tint = iconColor,
            size = 20.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Metric name
        Text(
            text = metricName,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        // Before value
        Text(
            text = String.format("%.1f", beforeValue),
            color = Color(0xFF8E8E93),
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Arrow
        Text(
            text = "→",
            color = Color(0xFF8E8E93),
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // After value
        Text(
            text = String.format("%.1f", afterValue),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Change
        Text(
            text = "${if (change > 0) "+" else ""}${String.format("%.1f", change)}",
            color = changeColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
