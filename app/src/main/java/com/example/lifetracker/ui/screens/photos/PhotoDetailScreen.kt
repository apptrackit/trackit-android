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
    val metricEntries = metrics.mapNotNull { (metric, unit) ->
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
                title = { Text("Photo Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
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
            // Photo display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Category badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x99000000),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = category.displayName,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Metrics overlay
                if (metricEntries.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x99000000),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            val basicMetrics = metricEntries.filter { (metric, _) ->
                                metric in listOf("Weight", "Height", "Body Fat")
                            }
                            basicMetrics.forEach { (metric, entry) ->
                                val (value, unit) = entry
                                Text(
                                    text = if (unit == "cm" && metric != "Height") {
                                        String.format("%s: %.1f %s", metric, value.value, unit)
                                    } else if (metric == "Height") {
                                        String.format("%s: %d %s", metric, value.value.toInt(), unit)
                                    } else {
                                        String.format("%s: %.1f%s", metric, value.value, unit)
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Date
            Text(
                text = date,
                color = Color(0xFFB3B3B3),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            )

            // Detailed metrics section
            if (metricEntries.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "DETAILED METRICS",
                            color = Color(0xFFB3B3B3),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Basic metrics
                        val basicMetrics = metricEntries.filter { (metric, _) ->
                            metric in listOf("Weight", "Height", "Body Fat")
                        }
                        basicMetrics.forEach { (metric, entry) ->
                            val (value, unit) = entry
                            MetricRow(
                                title = metric,
                                value = if (unit == "cm" && metric != "Height") {
                                    String.format("%.1f %s", value.value, unit)
                                } else if (metric == "Height") {
                                    String.format("%d %s", value.value.toInt(), unit)
                                } else {
                                    String.format("%.1f%s", value.value, unit)
                                }
                            )
                        }

                        // Body measurements
                        val bodyMeasurements = metricEntries.filter { (metric, _) ->
                            metric in listOf("Waist", "Bicep", "Chest", "Thigh", "Shoulder")
                        }

                        if (bodyMeasurements.isNotEmpty()) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFF2A2A2A)
                            )

                            Text(
                                text = "BODY MEASUREMENTS",
                                color = Color(0xFFB3B3B3),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                            )

                            bodyMeasurements.forEach { (metric, entry) ->
                                val (value, unit) = entry
                                MetricRow(
                                    title = metric,
                                    value = String.format("%.1f %s", value.value, unit)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category button
                Button(
                    onClick = {
                        val path = uri.path ?: return@Button
                        val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
                        navController.navigate(
                            PHOTO_CATEGORY_ROUTE.replace("{uri}", encodedPath)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.White
                    )
                ) {
                    Text("CHANGE CATEGORY")
                }

                // Notes button
                Button(
                    onClick = { showMetadataDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.White
                    )
                ) {
                    Text("ADD NOTES")
                }
            }

            // Compare button
            Button(
                onClick = { showPhotoSelectionDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A2A),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Compare",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("COMPARE WITH OTHER PHOTOS")
            }
        }
    }
}

@Composable
private fun MetricRow(
    title: String,
    value: String
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Divider(color = Color(0xFF2A2A2A), thickness = 0.5.dp)
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
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1A1A)
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
                        text = "Select Photo to Compare",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
                            color = Color.White,
                            fontSize = 16.sp,
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
                                        .background(Color(0xFF111111))
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
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1A1A)
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
                        text = "Add Notes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
                Text(
                    text = "NOTES",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF333333),
                        focusedContainerColor = Color(0xFF333333),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        cursorColor = Color(0xFF2196F3),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color(0xFF666666)
                    )
                )
                
                // Save button
                Button(
                    onClick = {
                        // Save only notes
                        val newMetadata = PhotoMetadata(
                            notes = notes
                        )
                        
                        photoViewModel.updatePhotoMetadata(context, photo, newMetadata)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("SAVE NOTES")
                }
            }
        }
    }
} 