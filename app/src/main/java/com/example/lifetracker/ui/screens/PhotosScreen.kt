package com.example.lifetracker.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.data.model.PhotoCategory
import com.example.lifetracker.ui.navigation.PHOTO_CATEGORY_ROUTE
import com.example.lifetracker.ui.navigation.PHOTO_DETAIL_ROUTE
import com.example.lifetracker.ui.theme.FontAwesomeIcon
import com.example.lifetracker.ui.theme.FontAwesomeIcons
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.ui.viewmodel.PhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotosScreen(
    navController: NavController,
    viewModel: HealthViewModel
) {
    val context = LocalContext.current
    val photoViewModel = remember { PhotoViewModel() }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, can proceed with camera/gallery
        }
    }

    // Gallery launcher with category selection
    var showCategorySelectionDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf(PhotoCategory.OTHER) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            showCategorySelectionDialog = true
        }
    }

    // Check and request permissions
    LaunchedEffect(Unit) {
        val permissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
            }
        }
    }

    // Load photos when screen is first shown
    LaunchedEffect(Unit) {
        photoViewModel.loadPhotos(context)
    }
    
    // Watch for category changes and apply filter
    LaunchedEffect(photoViewModel.selectedCategory) {
        photoViewModel.applyFilter()
    }
    
    // Category selection dialog for new photos
    if (showCategorySelectionDialog) {
        AlertDialog(
            onDismissRequest = { showCategorySelectionDialog = false },
            title = {
                Text(
                    text = "Select a Category",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    PhotoCategory.values().forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = category }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF2196F3)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.displayName,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedImageUri?.let {
                            photoViewModel.savePhoto(context, it, selectedCategory)
                        }
                        showCategorySelectionDialog = false
                    }
                ) {
                    Text("Save", color = Color(0xFF2196F3))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCategorySelectionDialog = false }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A),
            textContentColor = Color.White
        )
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
            // Header with title and add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Photos",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Add photo button
                FloatingActionButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    containerColor = Color(0xFF000000)
                ) {
                    FontAwesomeIcon(
                        icon = FontAwesomeIcons.Plus,
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All categories chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (photoViewModel.selectedCategory == null) Color(0xFF2196F3) else Color(0xFF333333),
                    modifier = Modifier
                        .clickable { photoViewModel.selectedCategory = null }
                ) {
                    Text(
                        text = "All",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                // Category chips
                PhotoCategory.values().forEach { category ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (photoViewModel.selectedCategory == category) Color(0xFF2196F3) else Color(0xFF333333),
                        modifier = Modifier
                            .clickable { photoViewModel.selectedCategory = category }
                    ) {
                        Text(
                            text = category.displayName,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Display the filtered photo grid
            if (photoViewModel.filteredPhotos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (photoViewModel.photos.isEmpty()) {
                            "No photos yet. Add your first photo with the + button."
                        } else {
                            "No photos in the '${photoViewModel.selectedCategory?.displayName}' category."
                        },
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                // Photo grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photoViewModel.filteredPhotos) { photo ->
                        val uri = photo.uri
                        val file = File(photo.filePath)
                        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(file.lastModified()))
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val filePath = photo.filePath
                                    val encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
                                    navController.navigate("photo_detail/$encodedPath")
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = rememberVectorPainter(Icons.Default.AccountBox),
                                    error = rememberVectorPainter(Icons.Default.Close)
                                )
                                
                                // Category badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x88000000),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = photo.category.displayName,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = date,
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