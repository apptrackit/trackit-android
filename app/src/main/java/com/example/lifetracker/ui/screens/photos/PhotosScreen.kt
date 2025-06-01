package com.example.lifetracker.ui.screens.photos

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import com.example.lifetracker.ui.theme.IconChoose
import com.guru.fontawesomecomposelib.FaIcon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.text.style.TextAlign

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
    
    // Greeting and date (dashboard style)
    val now = remember { Calendar.getInstance() }
    val greeting = remember {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val dateString = remember {
        val sdf = SimpleDateFormat("yyyy. MMM dd., EEEE", Locale.getDefault())
        sdf.format(now.time)
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

    // Group photos by year
    val photosByYear = remember(photoViewModel.filteredPhotos) {
        photoViewModel.filteredPhotos
            .groupBy { photo ->
                val file = File(photo.filePath)
                val cal = Calendar.getInstance().apply { timeInMillis = file.lastModified() }
                cal.get(Calendar.YEAR)
            }
            .toSortedMap(compareByDescending { it }) // Descending years
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF181818), Color(0xFF232323), Color.Black)
                )
            ),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp)
        ) {
            // Greeting header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateString,
                        color = Color(0xFFAAAAAA),
                        fontSize = 15.sp
                    )
                }
                // Add photo button
                FloatingActionButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    containerColor = Color(0xFF232323),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    FaIcon(
                        faIcon = com.guru.fontawesomecomposelib.FaIcons.Plus,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Category filter chips (dashboard style, more pill-like)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All categories chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (photoViewModel.selectedCategory == null) Color(0xFF2196F3) else Color(0xFF333333),
                    shadowElevation = if (photoViewModel.selectedCategory == null) 4.dp else 0.dp,
                    modifier = Modifier
                        .clickable { photoViewModel.selectedCategory = null }
                ) {
                    Text(
                        text = "All",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
                // Category chips with icons
                PhotoCategory.values().forEach { category ->
                    val (icon, color) = IconChoose.getIcon(category.displayName)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (photoViewModel.selectedCategory == category) color else Color(0xFF333333),
                        shadowElevation = if (photoViewModel.selectedCategory == category) 4.dp else 0.dp,
                        modifier = Modifier
                            .clickable { photoViewModel.selectedCategory = category }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(color.copy(alpha = 0.18f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                FaIcon(
                                    faIcon = icon,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.displayName,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Photos by year
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .padding(bottom = 16.dp)
                ) {
                    photosByYear.forEach { (year, photos) ->
                        item {
                            Text(
                                text = year.toString(),
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(start = 18.dp, top = 18.dp, bottom = 8.dp)
                            )
                        }
                        item {
                            // Fix: Use .toFloat() for the Int multiplication to avoid type mismatch
                            val gridHeight = ((photos.size + 1) / 2) * 220
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .heightIn(min = 0.dp, max = gridHeight.dp)
                            ) {
                                itemsIndexed(photos) { _, photo ->
                                    val uri = photo.uri
                                    val file = File(photo.filePath)
                                    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(file.lastModified()))
                                    val (icon, color) = IconChoose.getIcon(photo.category.displayName)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val filePath = photo.filePath
                                                val encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
                                                navController.navigate("photo_detail/$encodedPath")
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                                        elevation = CardDefaults.cardElevation(6.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(1f)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                            ) {
                                                AsyncImage(
                                                    model = uri,
                                                    contentDescription = "Photo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                // Floating date overlay
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .background(
                                                            color = Color.Black.copy(alpha = 0.55f),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = date,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FaIcon(
                                                    faIcon = icon,
                                                    tint = color,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = photo.category.displayName,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
