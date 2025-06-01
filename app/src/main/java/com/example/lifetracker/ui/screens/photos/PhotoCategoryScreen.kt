package com.example.lifetracker.ui.screens.photos

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lifetracker.data.model.Photo
import com.example.lifetracker.data.model.PhotoCategory
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.ui.viewmodel.PhotoViewModel
import java.io.File
import com.example.lifetracker.ui.theme.IconChoose
import com.guru.fontawesomecomposelib.FaIcon
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCategoryScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    photoUri: String
) {
    val context = LocalContext.current
    val photoViewModel = remember { PhotoViewModel() }

    // Decode the URL-encoded path
    val decodedPath = try {
        java.net.URLDecoder.decode(photoUri, "UTF-8")
    } catch (e: Exception) {
        photoUri
    }

    val file = File(decodedPath)
    val uri = Uri.fromFile(file)

    // Load photos to get current category
    LaunchedEffect(Unit) {
        photoViewModel.loadPhotos(context)
    }

    // Find the photo in the loaded photos
    val photo = photoViewModel.photos.find { it.filePath == decodedPath }
    var selectedCategory by remember { mutableStateOf(photo?.category ?: PhotoCategory.OTHER) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Category") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            photo?.let {
                                photoViewModel.updatePhotoCategory(context, it, selectedCategory)
                            }
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.Check, "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Display the photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF181818))
                    .padding(bottom = 18.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = "Select a category for this photo:",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp, top = 2.dp)
            )

            // Category selection, visually rich
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(PhotoCategory.values()) { category ->
                    val (icon, color) = IconChoose.getIcon(category.displayName)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selectedCategory = category },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategory == category) color.copy(alpha = 0.18f) else Color(0xFF232323)
                        ),
                        elevation = CardDefaults.cardElevation(if (selectedCategory == category) 4.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                FaIcon(
                                    faIcon = icon,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = category.displayName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = color,
                                    unselectedColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
