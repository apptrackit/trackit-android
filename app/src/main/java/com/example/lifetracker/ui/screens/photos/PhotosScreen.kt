package com.example.lifetracker.ui.screens.photos

import android.Manifest
import android.R.attr.contentDescription
import android.R.attr.tint
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImagePainter.State.Empty.painter

@Composable
fun PhotosScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
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
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
                        text = "Photos",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Add photo button styled exactly like dashboard

                IconButton(
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add),
                        contentDescription = "Add",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // --- Lowkey Category Chooser, centered icon above text ---
            val categories = listOf<Pair<String, PhotoCategory?>>(
                "All" to null
            ) + PhotoCategory.values().map { it.displayName to it }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { (label, category) ->
                    val isSelected = photoViewModel.selectedCategory == category
                    val (icon, color) = if (category == null)
                        IconChoose.getIcon("Other")
                    else
                        IconChoose.getIcon(category.displayName)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) color.copy(alpha = 0.13f) else Color.Transparent
                            )
                            .clickable { photoViewModel.selectedCategory = category }
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            FaIcon(
                                faIcon = icon,
                                tint = if (isSelected) color else Color(0xFFAAAAAA),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            color = if (isSelected) color else Color(0xFFAAAAAA),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .widthIn(min = 40.dp, max = 70.dp)
                                .align(Alignment.CenterHorizontally)
                        )
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
                                    val date = SimpleDateFormat(
                                        "MMM dd, yyyy",
                                        Locale.getDefault()
                                    ).format(Date(file.lastModified()))
                                    val (icon, color) = IconChoose.getIcon(photo.category.displayName)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val filePath = photo.filePath
                                                val encodedPath =
                                                    java.net.URLEncoder.encode(
                                                        filePath,
                                                        "UTF-8"
                                                    )
                                                navController.navigate("photo_detail/$encodedPath")
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(
                                                0xFF181818
                                            )
                                        ),
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
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = 16.dp,
                                                            topEnd = 16.dp
                                                        )
                                                    )
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
                                                        .padding(
                                                            horizontal = 10.dp,
                                                            vertical = 4.dp
                                                        )
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
                                                    .padding(
                                                        horizontal = 10.dp,
                                                        vertical = 8.dp
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                FaIcon(
                                                    faIcon = icon,
                                                    tint = color,
                                                    modifier = Modifier.size(22.dp)
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
