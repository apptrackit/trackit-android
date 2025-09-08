package com.ballabotond.trackit.ui.screens.photos

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
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
//import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ballabotond.trackit.data.model.Photo
import com.ballabotond.trackit.data.model.PhotoCategory
import com.ballabotond.trackit.data.model.PhotoMetadata
import com.ballabotond.trackit.ui.navigation.PHOTO_CATEGORY_ROUTE
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.viewmodel.PhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import com.ballabotond.trackit.data.model.HistoryEntry
import android.app.DatePickerDialog
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.zIndex
import com.ballabotond.trackit.ui.theme.IconChoose
import com.ballabotond.trackit.ui.theme.FeatherIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    navController: NavController,
    healthViewModel: HealthViewModel,
    photoViewModel: PhotoViewModel,
    photoUri: String
) {
    val context = LocalContext.current

    // For photo selection dialog
    var showPhotoSelectionDialog by remember { mutableStateOf(false) }
    var showMetadataDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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

    // Find current photo and its category
    val photo = photoViewModel.photos.find { it.filePath == decodedPath }
    val category = photo?.category ?: PhotoCategory.OTHER

    // Date state for editing
    var dateMillis by remember { mutableStateOf(file.lastModified()) }
    
    // Photo zoom/pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val date = try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dateMillis))
    } catch (e: Exception) {
        "Unknown date"
    }

    // Date picker dialog
    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth, 12, 0, 0)
                val newDateMillis = newCal.timeInMillis
                dateMillis = newDateMillis
                // Update file lastModified and metadata timestamp
                photo?.let {
                    photoViewModel.updatePhotoDate(context, it, newDateMillis)
                }
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
        // Prevent dialog from showing twice
        showDatePicker = false
    }

    // Calculate metric entries
    val photoDate = file.lastModified()
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

    // Get the latest entry before or on the photo date for each metric
    val metricEntries = metrics.map { (metric, unit) ->
        val history = healthViewModel.getMetricHistory(metric, unit)
        val entriesBeforePhoto = history.filter { it.date <= photoDate }
        val latestEntry = entriesBeforePhoto.maxByOrNull { it.date }
        
        Log.d("PhotoMetrics", "Metric: $metric, Found entries: ${entriesBeforePhoto.size}, Latest: ${latestEntry?.value}")
        
        metric to (latestEntry to unit)
    }
    
    if (metricEntries.isEmpty()) {
        Log.d("PhotoMetrics", "No metrics found for photo date ${Date(photoDate)}")
    } else {
        Log.d("PhotoMetrics", "Found ${metricEntries.size} metrics for photo date ${Date(photoDate)}")
    }

    // Photo selection dialog
    if (showPhotoSelectionDialog) {
        PhotoSelectionDialog(
            onDismiss = { showPhotoSelectionDialog = false },
            photoViewModel = photoViewModel,
            currentPhoto = photo,
            currentPhotoPath = decodedPath,
            navController = navController
        )
    }

    // Metadata dialog
    if (showMetadataDialog && photo != null) {
        MetadataDialog(
            onDismiss = { showMetadataDialog = false },
            photo = photo,
            photoViewModel = photoViewModel,
            context = context
        )
    }

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
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (photo != null) {
                                photoViewModel.deletePhoto(context, photo)
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Photo",
                            tint = Color(0xFFE57373)
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
            // Category filter tabs - like in the compare screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryTab("All Phot...", "All", true, Color(0xFF007AFF))
                CategoryTab("Front", "Front", category == PhotoCategory.FRONT, if (category == PhotoCategory.FRONT) Color(0xFF007AFF) else Color.Gray)
                CategoryTab("Side", "Side", category == PhotoCategory.SIDE, if (category == PhotoCategory.SIDE) Color(0xFF007AFF) else Color.Gray)
                CategoryTab("Back", "Back", category == PhotoCategory.BACK, if (category == PhotoCategory.BACK) Color(0xFF007AFF) else Color.Gray)
                CategoryTab("Arms", "Biceps", category == PhotoCategory.BICEPS, if (category == PhotoCategory.BICEPS) Color(0xFF007AFF) else Color.Gray)
                CategoryTab("Chest", "Chest", category == PhotoCategory.CHEST, if (category == PhotoCategory.CHEST) Color(0xFF007AFF) else Color.Gray)
            }
            
            // Main content card
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
                    // Photo section
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.75f)
                                .clip(RoundedCornerShape(12.dp))
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
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date display
                    Text(
                        text = date,
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change Date", fontSize = 14.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                val path = uri.path ?: return@OutlinedButton
                                val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
                                navController.navigate(
                                    PHOTO_CATEGORY_ROUTE.replace("{uri}", encodedPath)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Category", fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showMetadataDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Notes", fontSize = 14.sp)
                        }
                        
                        OutlinedButton(
                            onClick = { showPhotoSelectionDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Compare", fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Metrics section
                    val metricsWithData = metricEntries.filter { it.second.first != null }
                    if (metricsWithData.isNotEmpty()) {
                        metricsWithData.forEach { (metricName, entryData) ->
                            val entry = entryData.first!!
                            val unit = entryData.second
                            
                            MetricDetailRow(
                                metricName = metricName,
                                value = entry.value.toDouble(),
                                unit = unit
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for navigation
        }
    }
}

@Composable
private fun MetricRow(
    title: String,
    value: String
) {
    val (icon, color) = IconChoose.getIcon(title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FeatherIcon(
            icon = icon,
            tint = color,
            size = 18.dp // Bigger icon in row
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PhotoSelectionDialog(
    onDismiss: () -> Unit,
    photoViewModel: PhotoViewModel,
    currentPhoto: Photo?,
    currentPhotoPath: String,
    navController: NavController
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF000000)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPARE",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Only show photos of the same category
                val category = currentPhoto?.category ?: PhotoCategory.OTHER
                val sameTypePhotos = photoViewModel.getPhotosOfCategory(category)
                    .filter { it.filePath != currentPhotoPath }

                if (sameTypePhotos.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No other photos of type '${category.displayName}' to compare with",
                            color = Color(0xFFB3B3B3),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Grid of photos
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(sameTypePhotos) { comparePhoto ->
                            val compareUri = comparePhoto.uri
                            val compareFile = File(comparePhoto.filePath)
                            val compareDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(compareFile.lastModified()))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()

                                        // Navigate to comparison screen
                                        val mainPath = currentPhotoPath
                                        val comparePath = comparePhoto.filePath

                                        val encodedMainPath = java.net.URLEncoder.encode(mainPath, "UTF-8")
                                        val encodedComparePath = java.net.URLEncoder.encode(comparePath, "UTF-8")

                                        navController.navigate("photo_compare/$encodedMainPath/$encodedComparePath")
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
                                    color = Color(0xFFB3B3B3),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetadataDialog(
    onDismiss: () -> Unit,
    photo: Photo,
    photoViewModel: PhotoViewModel,
    context: Context
) {
    var notes by remember { mutableStateOf(photo.metadata.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF000000)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NOTES",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                
                // Notes input
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF1A1A1A),
                        focusedContainerColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        cursorColor = Color(0xFFB3B3B3),
                        focusedIndicatorColor = Color(0xFFB3B3B3),
                        unfocusedIndicatorColor = Color(0xFF2A2A2A)
                    )
                )
                
                // Save button
                OutlinedButton(
                    onClick = {
                        // Save only notes
                        val newMetadata = PhotoMetadata(
                            notes = notes
                        )
                        
                        photoViewModel.updatePhotoMetadata(context, photo, newMetadata)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color(0xFF2A2A2A))
                    )
                ) {
                    Text("SAVE")
                }
            }
        }
    }
}

@Composable
private fun MetricsSection(
    metricEntries: List<Pair<String, Pair<HistoryEntry, String>>>,
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
            val entry = metricEntries.find { it.first == metricName }
            
            MetricRow(
                title = metricName,
                value = entry?.second?.first?.value?.let { formatValue(it, unit) } ?: "-"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun formatValue(value: Float, unit: String): String {
    return when {
        unit == "cm" && value != value.toInt().toFloat() -> String.format("%.1f %s", value, unit)
        unit == "cm" -> String.format("%d %s", value.toInt(), unit)
        unit == "kg" -> String.format("%.1f %s", value, unit)
        unit == "%" -> String.format("%.1f%s", value, unit)
        else -> String.format("%.1f %s", value, unit)
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
private fun MetricDetailRow(
    metricName: String,
    value: Double,
    unit: String
) {
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
        
        // Value
        Text(
            text = String.format("%.1f %s", value, unit),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
