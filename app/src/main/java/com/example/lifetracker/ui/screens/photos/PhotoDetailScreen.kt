package com.example.lifetracker.ui.screens.photos

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.data.model.Photo
import com.example.lifetracker.data.model.PhotoCategory
import com.example.lifetracker.data.model.PhotoMetadata
import com.example.lifetracker.ui.navigation.PHOTO_CATEGORY_ROUTE
import com.example.lifetracker.ui.navigation.PHOTO_COMPARE_ROUTE
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.ui.viewmodel.PhotoViewModel
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
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.screens.photos.MetricRow

@OptIn(ExperimentalMaterial3Api::class)
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
    var showMetadataDialog by remember { mutableStateOf(false) }

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

    val date = try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(file.lastModified()))
    } catch (e: Exception) {
        "Unknown date"
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
        val history = viewModel.getMetricHistory(metric, unit)
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
                title = { Text("") },
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
                    containerColor = Color(0xFF000000),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF000000)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            // Photo display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo",
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
                                onGesture = { centroid: Offset, pan: Offset, zoom: Float, rotation: Float ->
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

            // Date and Category
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    color = Color(0xFFB3B3B3),
                    fontSize = 14.sp
                )
                Text(
                    text = category.displayName,
                    color = Color(0xFFB3B3B3),
                    fontSize = 14.sp
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category button
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
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color(0xFF2A2A2A))
                    )
                ) {
                    Text("CATEGORY")
                }

                // Notes button
                OutlinedButton(
                    onClick = { showMetadataDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color(0xFF2A2A2A))
                    )
                ) {
                    Text("NOTES")
                }
            }

            // Compare button
            OutlinedButton(
                onClick = { showPhotoSelectionDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(Color(0xFF2A2A2A))
                )
            ) {
                Text("COMPARE")
            }

            // Metrics section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1A1A1A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Metrics rows
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
                        val value = entry?.second?.first?.value
                        val valueString = value?.let { 
                            if (unit == "cm" && metricName != "Height") {
                                String.format("%.1f", it)
                            } else if (metricName == "Height") {
                                String.format("%d", it.toInt())
                            } else {
                                String.format("%.1f", it)
                            }
                        } ?: "-"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = metricName,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            
                            Text(
                                text = "$valueString ${if (valueString != "-") unit else ""}",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                        
                        if (metricName != allMetrics.last().first) {
                            Divider(color = Color(0xFF333333), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp
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