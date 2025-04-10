package com.example.lifetracker.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.ui.navigation.PHOTO_DETAIL_ROUTE
import com.example.lifetracker.ui.theme.FontAwesomeIcons
import com.example.lifetracker.ui.theme.FontAwesomeIcon
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.ui.viewmodel.PhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource

@Composable
fun PhotosScreen(
    navController: NavController,
    viewModel: HealthViewModel
) {
    val context = LocalContext.current
    val photoViewModel = remember { PhotoViewModel() }
    
    // State for photo comparison
    var showComparisonMode by remember { mutableStateOf(false) }
    var leftPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var rightPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectingForLeftWindow by remember { mutableStateOf(true) } // true = selecting for left, false = for right
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, can proceed with camera/gallery
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoViewModel.savePhoto(context, it)
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Compare toggle button
                    OutlinedButton(
                        onClick = { showComparisonMode = !showComparisonMode },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showComparisonMode) Color(0xFF2196F3) else Color.Transparent,
                            contentColor = if (showComparisonMode) Color.White else Color(0xFF2196F3)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = SolidColor(Color(0xFF2196F3))
                        )
                    ) {
                        FontAwesomeIcon(
                            icon = FontAwesomeIcons.Images,
                            tint = if (showComparisonMode) Color.White else Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compare")
                    }
                    
                    // Add photo button
                    FloatingActionButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        containerColor = Color(0xFF2196F3)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Photo",
                            tint = Color.White
                        )
                    }
                }
            }

            // Photo comparison UI
            AnimatedVisibility(
                visible = showComparisonMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Photo Comparison",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left photo window
                        ComparisonPhotoWindow(
                            uri = leftPhotoUri,
                            isSelected = selectingForLeftWindow,
                            onSelect = { 
                                selectingForLeftWindow = true 
                            },
                            onClear = { leftPhotoUri = null },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Right photo window
                        ComparisonPhotoWindow(
                            uri = rightPhotoUri,
                            isSelected = !selectingForLeftWindow,
                            onSelect = { 
                                selectingForLeftWindow = false 
                            },
                            onClear = { rightPhotoUri = null },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Photo grid with selection functionality
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photoViewModel.photos) { uri ->
                    val file = File(uri.path ?: "")
                    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(file.lastModified()))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (showComparisonMode) {
                                    // In comparison mode, populate selected window
                                    if (selectingForLeftWindow) {
                                        leftPhotoUri = uri
                                    } else {
                                        rightPhotoUri = uri
                                    }
                                } else {
                                    // Normal mode - navigate to detail
                                    val filePath = uri.path ?: return@clickable
                                    val encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
                                    navController.navigate("photo_detail/$encodedPath")
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .then(
                                    if (uri == leftPhotoUri || uri == rightPhotoUri) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = if (uri == leftPhotoUri) Color.Blue else Color.Green
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = rememberVectorPainter(Icons.Default.AccountBox),
                                error = rememberVectorPainter(Icons.Default.Close)
                            )
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

@Composable
fun ComparisonPhotoWindow(
    uri: Uri?,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFF333333))
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF2196F3) else Color(0xFF666666)
            )
            .clickable(onClick = onSelect)
    ) {
        if (uri != null) {
            // Show the selected photo
            AsyncImage(
                model = uri,
                contentDescription = "Comparison Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Clear button
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Photo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSelected) "Select a photo below" else "Click to select",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
} 