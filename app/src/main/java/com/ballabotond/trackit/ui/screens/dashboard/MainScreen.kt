package com.ballabotond.trackit.ui.screens.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ballabotond.trackit.data.model.PhotoCategory
import com.ballabotond.trackit.ui.navigation.*
import com.ballabotond.trackit.ui.theme.FeatherIconsCollection
import com.ballabotond.trackit.ui.theme.FeatherIcon
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.viewmodel.PhotoViewModel
import com.google.accompanist.pager.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import com.ballabotond.trackit.ui.screens.health.ProgressScreen
import com.ballabotond.trackit.ui.screens.photos.PhotosScreen

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    syncViewModel: com.ballabotond.trackit.ui.viewmodel.SyncViewModel? = null,
    photoViewModel: PhotoViewModel? = null
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Gallery launcher for adding photos from dashboard
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

    // Category selection dialog for photos added from dashboard
    if (showCategorySelectionDialog) {
        AlertDialog(
            onDismissRequest = { showCategorySelectionDialog = false },
            title = {
                Text(
                    text = "Select a Category",
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                            photoViewModel?.savePhoto(context, it, selectedCategory)
                        }
                        showCategorySelectionDialog = false
                        selectedImageUri = null
                        // Navigate to photos tab to see the new photo
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                ) {
                    Text("Save", color = Color(0xFF2196F3))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCategorySelectionDialog = false
                        selectedImageUri = null
                    }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF2C2C2E)
        )
    }

    val pages = listOf(
        NavigationItem(DASHBOARD_ROUTE, FeatherIconsCollection.Home, "Dashboard"),
        NavigationItem(PROGRESS_ROUTE, FeatherIconsCollection.TrendingUp, "Metrics"),
        NavigationItem(PHOTOS_ROUTE, FeatherIconsCollection.Camera, "Photos")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                tonalElevation = 0.dp,
                modifier = Modifier.height(88.dp) // Increased height to match mockup
            ) {
                pages.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FeatherIcon(
                                        icon = item.icon, 
                                        tint = if (pagerState.currentPage == index) Color(0xFF34C759) else Color(0xFF8E8E93),
                                        size = 24.dp
                                    )
                                }
                            }
                        },
                        label = { 
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                color = if (pagerState.currentPage == index) Color(0xFF34C759) else Color(0xFF8E8E93),
                                fontWeight = if (pagerState.currentPage == index) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal
                            ) 
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF34C759),
                            selectedTextColor = Color(0xFF34C759),
                            unselectedIconColor = Color(0xFF8E8E93),
                            unselectedTextColor = Color(0xFF8E8E93),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) { page ->
            when (page) {
                0 -> {
                    DashboardScreen(
                        navController = navController,
                        viewModel = viewModel,
                        syncViewModel = syncViewModel,
                        onNavigateToEditMetric = { metricName ->
                            when (metricName) {
                                "Weight" -> navController.navigate("edit_metric/Weight/kg/Weight")
                                "Height" -> navController.navigate("edit_metric/Height/cm/Height")
                                "Body Fat" -> navController.navigate("edit_metric/Body Fat/%/Body Fat")
                                else -> navController.navigate("edit_metric/$metricName/unit/$metricName")
                            }
                        },
                        onNavigateToViewBMIHistory = { 
                            navController.navigate("view_bmi_history")
                        },
                        onNavigateToPhotos = {
                            scope.launch {
                                pagerState.animateScrollToPage(2) // Navigate to photos tab
                            }
                        },
                        onLaunchGallery = {
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
                1 -> {
                    ProgressScreen(
                        navController = navController,
                        viewModel = viewModel,
                        onNavigateToEditMetric = { metricName ->
                            when (metricName) {
                                "Weight" -> navController.navigate("edit_metric/Weight/kg/Weight")
                                "Height" -> navController.navigate("edit_metric/Height/cm/Height")
                                "Body Fat" -> navController.navigate("edit_metric/Body Fat/%/Body Fat")
                                "Waist" -> navController.navigate("edit_metric/Waist/cm/Waist")
                                "Bicep" -> navController.navigate("edit_metric/Bicep/cm/Bicep")
                                "Chest" -> navController.navigate("edit_metric/Chest/cm/Chest")
                                "Thigh" -> navController.navigate("edit_metric/Thigh/cm/Thigh")
                                "Shoulder" -> navController.navigate("edit_metric/Shoulder/cm/Shoulder")
                                else -> navController.navigate("edit_metric/$metricName/unit/$metricName")
                            }
                        },
                        onNavigateToViewBMIHistory = { 
                            navController.navigate("view_bmi_history")
                        }
                    )
                }
                2 -> {
                    PhotosScreen(
                        navController = navController,
                        photoViewModel = photoViewModel,
                        healthViewModel = viewModel
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)
