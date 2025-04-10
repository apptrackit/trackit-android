package com.example.lifetracker.ui.screens

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
                title = { Text(date) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        photo?.let {
                            photoViewModel.deletePhoto(context, it)
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
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
            // Photo display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFB600),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = category.displayName,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                
                    // Photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(Color(0xFF111111))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            // Metadata display if available
            photo?.metadata?.let { metadata ->
                if (metadata.weight != null || metadata.bodyFatPercentage != null || 
                    metadata.measurements.isNotEmpty() || metadata.notes.isNotEmpty()) {
                    
                    MetadataCard(metadata = metadata)
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
                        containerColor = Color(0xFFFFB600)
                    )
                ) {
                    Text("CHANGE CATEGORY")
                }

                // Metadata button
                Button(
                    onClick = { showMetadataDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("EDIT METADATA")
                }
            }
            
            // Compare button
            Button(
                onClick = { showPhotoSelectionDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
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

@Composable
private fun MetadataCard(metadata: PhotoMetadata) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "PHOTO METADATA",
                color = Color(0xFF2196F3),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Weight
            metadata.weight?.let { weight ->
                MetadataRow(
                    label = "Weight",
                    value = String.format("%.1f kg", weight)
                )
            }
            
            // Body fat
            metadata.bodyFatPercentage?.let { bodyFat ->
                MetadataRow(
                    label = "Body Fat",
                    value = String.format("%.1f%%", bodyFat)
                )
            }
            
            // Measurements
            if (metadata.measurements.isNotEmpty()) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFF333333)
                )
                
                Text(
                    text = "MEASUREMENTS",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                )
                
                metadata.measurements.forEach { (key, value) ->
                    MetadataRow(
                        label = key.replaceFirstChar { it.uppercase() },
                        value = String.format("%.1f cm", value)
                    )
                }
            }
            
            // Notes
            if (metadata.notes.isNotEmpty()) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFF333333)
                )
                
                Text(
                    text = "NOTES",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                )
                
                Text(
                    text = metadata.notes,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 14.sp
        )
        
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
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
    var weight by remember { mutableStateOf(photo.metadata.weight?.toString() ?: "") }
    var bodyFat by remember { mutableStateOf(photo.metadata.bodyFatPercentage?.toString() ?: "") }
    var notes by remember { mutableStateOf(photo.metadata.notes) }
    
    // For measurements
    val measurements = remember { photo.metadata.measurements.toMutableMap() }
    
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
                        text = "Edit Photo Metadata",
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
                
                // Weight input
                Text(
                    text = "WEIGHT (kg)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                TextField(
                    value = weight,
                    onValueChange = { weight = it },
                modifier = Modifier
                    .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF333333),
                        focusedContainerColor = Color(0xFF333333),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        cursorColor = Color(0xFF2196F3),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color(0xFF666666)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                // Body fat input
                Text(
                    text = "BODY FAT (%)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                TextField(
                    value = bodyFat,
                    onValueChange = { bodyFat = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF333333),
                        focusedContainerColor = Color(0xFF333333),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        cursorColor = Color(0xFF2196F3),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color(0xFF666666)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                // Measurements section
                Text(
                    text = "MEASUREMENTS (cm)",
                    color = Color(0xFF2196F3),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                
                // Common measurements inputs
                val commonMeasurements = listOf("chest", "waist", "hips", "biceps", "thighs", "calves", "shoulders")
                
                commonMeasurements.forEach { measurementKey ->
                    Text(
                        text = measurementKey.replaceFirstChar { it.uppercase() },
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    val initialValue = measurements[measurementKey]?.toString() ?: ""
                    var value by remember { mutableStateOf(initialValue) }
                    
                    TextField(
                        value = value,
                        onValueChange = { 
                            value = it
                            if (it.isNotEmpty()) {
                                try {
                                    measurements[measurementKey] = it.toFloat()
                                } catch (e: Exception) {
                                    // Invalid input, don't update
                                }
                            } else {
                                measurements.remove(measurementKey)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF333333),
                            focusedContainerColor = Color(0xFF333333),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            cursorColor = Color(0xFF2196F3),
                            focusedIndicatorColor = Color(0xFF2196F3),
                            unfocusedIndicatorColor = Color(0xFF666666)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
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
                        // Save metadata
                        val weightValue = weight.toFloatOrNull()
                        val bodyFatValue = bodyFat.toFloatOrNull()
                        
                        val newMetadata = PhotoMetadata(
                            weight = weightValue,
                            bodyFatPercentage = bodyFatValue,
                            notes = notes,
                            measurements = measurements
                        )
                        
                        photoViewModel.updatePhotoMetadata(context, photo, newMetadata)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("SAVE METADATA")
                }
            }
        }
    }
} 