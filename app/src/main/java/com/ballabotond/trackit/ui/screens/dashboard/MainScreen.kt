package com.ballabotond.trackit.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ballabotond.trackit.ui.navigation.*
import com.ballabotond.trackit.ui.common.AppIcon // Correct import
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.viewmodel.PhotoViewModel
import com.google.accompanist.pager.*
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
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    val pages = listOf(
        NavigationItem(DASHBOARD_ROUTE, "home", "Dashboard"),
        NavigationItem(PROGRESS_ROUTE, "monitoring", "Progress"),
                NavigationItem(PHOTOS_ROUTE, "photo_library", "Photos")

    )

    // This state helps sync the bottom bar with page swipes
    val selectedPageIndex by remember { derivedStateOf { pagerState.currentPage } }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1A1A),
                tonalElevation = 0.dp // Usually 0dp for a clean modern look
            ) {
                pages.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedPageIndex == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            AppIcon(
                                iconName = item.icon,
                                // The tint is handled by `colors` below, so we don't need to set it manually
                                modifier = Modifier.size(24.dp), // Standard M3 icon size
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 12.sp // Standard M3 label size
                            )
                        },
                        alwaysShowLabel = true, // Ensures labels are always visible
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2196F3),
                            selectedTextColor = Color(0xFF2196F3),
                            unselectedIconColor = Color.White.copy(alpha = 0.8f),
                            unselectedTextColor = Color.White.copy(alpha = 0.8f),
                            indicatorColor = Color.Transparent // Removes the highlight circle/pill
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding here to the content
        ) { page ->
            when (pages[page].route) { // Use route from the list for safety
                DASHBOARD_ROUTE -> DashboardScreen(
                    onNavigateToEditMetric = { metricName ->
                        when (metricName) {
                            "Weight" -> navController.navigate(EDIT_WEIGHT_ROUTE)
                            "Height" -> navController.navigate(EDIT_HEIGHT_ROUTE)
                            "Body Fat" -> navController.navigate(EDIT_BODY_FAT_ROUTE)
                            else -> navController.navigate(
                                EDIT_METRIC_ROUTE
                                    .replace("{metricName}", metricName)
                                    .replace("{unit}", "cm")
                                    .replace("{title}", metricName)
                            )
                        }
                    },
                    onNavigateToViewBMIHistory = { navController.navigate(VIEW_BMI_HISTORY_ROUTE) },
                    viewModel = viewModel,
                    navController = navController,
                    syncViewModel = syncViewModel
                )
                PHOTOS_ROUTE -> PhotosScreen(
                    navController = navController,
                    healthViewModel = viewModel,
                    syncRepository = viewModel.syncRepository,
                    photoViewModel = photoViewModel
                )
                PROGRESS_ROUTE -> ProgressScreen(
                    onNavigateToEditMetric = { metricName ->
                        when (metricName) {
                            "Weight" -> navController.navigate(EDIT_WEIGHT_ROUTE)
                            "Height" -> navController.navigate(EDIT_HEIGHT_ROUTE)
                            "Body Fat" -> navController.navigate(EDIT_BODY_FAT_ROUTE)
                            else -> navController.navigate(
                                EDIT_METRIC_ROUTE
                                    .replace("{metricName}", metricName)
                                    .replace("{unit}", "cm")
                                    .replace("{title}", metricName)
                            )
                        }
                    },
                    onNavigateToViewBMIHistory = { navController.navigate(VIEW_BMI_HISTORY_ROUTE) },
                    viewModel = viewModel,
                    navController = navController,
                    syncViewModel = syncViewModel
                )
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val icon: String,
    val label: String
)