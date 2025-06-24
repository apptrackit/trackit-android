package com.ballabotond.trackit.ui.screens.photos

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ballabotond.trackit.data.model.PhotoCategory
import com.ballabotond.trackit.data.model.PhotoMetadata
import com.ballabotond.trackit.data.model.HistoryEntry
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.viewmodel.PhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import kotlin.math.abs
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import com.ballabotond.trackit.ui.theme.IconChoose
import com.guru.fontawesomecomposelib.FaIcon


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
    
    // --- Ensure left is always the older photo, right is newer ---
    val (leftPhoto, rightPhoto) = if (mainFile.lastModified() <= compareFile.lastModified()) {
        Pair(mainPhoto to mainFile, comparePhoto to compareFile)
    } else {
        Pair(comparePhoto to compareFile, mainPhoto to mainFile)
    }
    val leftUri = Uri.fromFile(leftPhoto.second)
    val rightUri = Uri.fromFile(rightPhoto.second)
    val leftCategory = leftPhoto.first?.category ?: PhotoCategory.OTHER
    val rightCategory = rightPhoto.first?.category ?: PhotoCategory.OTHER
    val leftDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        .format(Date(leftPhoto.second.lastModified()))
    val rightDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        .format(Date(rightPhoto.second.lastModified()))
    val leftMetadata = leftPhoto.first?.metadata ?: PhotoMetadata()
    val rightMetadata = rightPhoto.first?.metadata ?: PhotoMetadata()

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
    
    val leftMetricEntries = getPhotoMetrics(leftPhoto.second)
    val rightMetricEntries = getPhotoMetrics(rightPhoto.second)
    
    // Log metric entries for debugging
    Log.d("PhotoMetrics", "Main photo (${leftDate}): Found ${leftMetricEntries.size} metrics")
    leftMetricEntries.forEach { (metric, data) ->
        val (entry, unit) = data
        Log.d("PhotoMetrics", "  $metric: ${entry.value} $unit (recorded on ${Date(entry.date)})")
    }
    
    Log.d("PhotoMetrics", "Compare photo (${rightDate}): Found ${rightMetricEntries.size} metrics")
    rightMetricEntries.forEach { (metric, data) ->
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
                title = { Text("Progress Photos") },
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
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color(0xFF181818), Color(0xFF232323), Color.Black)
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Comparison",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    // Photos with category badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left (older) photo
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val (icon1, color1) = IconChoose.getIcon(leftCategory.displayName)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = color1.copy(alpha = 0.92f),
                                shadowElevation = 2.dp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    FaIcon(
                                        faIcon = icon1,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp) // Bigger icon in badge
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = leftCategory.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .aspectRatio(3f / 4f)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                var scale by remember { mutableStateOf(1f) }
                                var offset by remember { mutableStateOf(Offset.Zero) }
                                AsyncImage(
                                    model = leftUri,
                                    contentDescription = "Older Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x.coerceIn(-200f, 200f),
                                            translationY = offset.y.coerceIn(-200f, 200f)
                                        )
                                        .pointerInput(Unit) {
                                            detectTransformGestures(
                                                onGesture = { _, pan, zoom, _ ->
                                                    scale = (scale * zoom).coerceIn(0.5f, 4f)
                                                    offset += pan
                                                    val maxOffset = 200f * (scale - 0.5f)
                                                    offset = Offset(
                                                        offset.x.coerceIn(-maxOffset, maxOffset),
                                                        offset.y.coerceIn(-maxOffset, maxOffset)
                                                    )
                                                }
                                            )
                                        },
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Text(
                                text = leftDate,
                                color = Color(0xFFB3B3B3),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        // Right (newer) photo
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val (icon2, color2) = IconChoose.getIcon(rightCategory.displayName)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = color2.copy(alpha = 0.92f),
                                shadowElevation = 2.dp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    FaIcon(
                                        faIcon = icon2,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp) // Bigger icon in badge
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = rightCategory.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .aspectRatio(3f / 4f)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                var scale by remember { mutableStateOf(1f) }
                                var offset by remember { mutableStateOf(Offset.Zero) }
                                AsyncImage(
                                    model = rightUri,
                                    contentDescription = "Newer Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x.coerceIn(-200f, 200f),
                                            translationY = offset.y.coerceIn(-200f, 200f)
                                        )
                                        .pointerInput(Unit) {
                                            detectTransformGestures(
                                                onGesture = { _, pan, zoom, _ ->
                                                    scale = (scale * zoom).coerceIn(0.5f, 4f)
                                                    offset += pan
                                                    val maxOffset = 200f * (scale - 0.5f)
                                                    offset = Offset(
                                                        offset.x.coerceIn(-maxOffset, maxOffset),
                                                        offset.y.coerceIn(-maxOffset, maxOffset)
                                                    )
                                                }
                                            )
                                        },
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Text(
                                text = rightDate,
                                color = Color(0xFFB3B3B3),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    // Metrics comparison
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232323)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        MetricsComparison(
                            mainMetricEntries = leftMetricEntries,
                            compareMetricEntries = rightMetricEntries,
                            modifier = Modifier.padding(18.dp)
                        )
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

@Composable
private fun MetricsComparison(
    mainMetricEntries: List<Pair<String, Pair<HistoryEntry, String>>>,
    compareMetricEntries: List<Pair<String, Pair<HistoryEntry, String>>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Define all possible metrics in order
        val allMetrics = listOf(
            "Weight" to "kg",
            "Height" to "cm",
            "Body Fat" to "%",
            "Waist" to "cm",
            "Bicep" to "cm",
            "Chest" to "cm",
            "Thigh" to "cm",
            "Shoulder" to "cm"
        )
        
        allMetrics.forEach { (metricName, unit) ->
            val mainEntry = mainMetricEntries.find { it.first == metricName }
            val compareEntry = compareMetricEntries.find { it.first == metricName }
            
            MetricRow(
                metricName = metricName,
                beforeValue = mainEntry?.second?.first?.value,
                afterValue = compareEntry?.second?.first?.value,
                unit = unit
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
internal fun MetricRow(
    metricName: String,
    beforeValue: Float?,
    afterValue: Float?,
    unit: String
) {
    val (icon, color) = IconChoose.getIcon(metricName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon based on metric type
        FaIcon(
            faIcon = icon,
            tint = color,
            modifier = Modifier
                .size(20.dp) // Bigger icon in row
                .padding(end = 10.dp)
        )
        
        // Metric name
        Text(
            text = metricName,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        // Values and difference
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Before value
            Text(
                text = beforeValue?.let { formatValue(it, unit) } ?: "-",
                color = Color.Gray,
                fontSize = 16.sp
            )
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "to",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            
            // After value
            Text(
                text = afterValue?.let { formatValue(it, unit) } ?: "-",
                color = Color.Gray,
                fontSize = 16.sp
            )
            
            // Difference indicator
            if (beforeValue != null && afterValue != null) {
                val difference = afterValue - beforeValue
                val formattedDiff = String.format("%.1f", abs(difference))
                val color = when {
                    difference > 0 -> Color(0xFF4CAF50) // Green
                    difference < 0 -> Color(0xFFE57373) // Red
                    else -> Color.Gray
                }
                val sign = when {
                    difference > 0 -> "+"
                    difference < 0 -> "-"
                    else -> ""
                }
                
                Text(
                    text = "$sign$formattedDiff",
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun formatValue(value: Float, unit: String): String {
    return when {
        unit == "cm" -> String.format("%.1f", value)
        unit == "kg" -> String.format("%.1f", value)
        unit == "%" -> String.format("%.1f", value)
        else -> String.format("%.1f", value)
    }
}
