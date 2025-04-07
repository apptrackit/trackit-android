package com.example.lifetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lifetracker.ui.navigation.*
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: HealthViewModel
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pages = listOf(
        NavigationItem(DASHBOARD_ROUTE, Icons.Default.Home, "Dashboard"),
        NavigationItem(NUTRITION_ROUTE, Icons.Default.List, "Nutrition"),
        NavigationItem(WORKOUT_ROUTE, Icons.Default.Star, "Workout"),
        NavigationItem(PROGRESS_ROUTE, Icons.Default.Info, "Progress"),
        NavigationItem(PROFILE_ROUTE, Icons.Default.Person, "Profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1A1A),
                tonalElevation = 8.dp
            ) {
                pages.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { 
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                maxLines = 1
                            ) 
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2196F3),
                            selectedTextColor = Color(0xFF2196F3),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF1A1A1A)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DashboardScreen(
                        onNavigateToEditMetric = { metricName ->
                            when (metricName) {
                                "Weight" -> navController.navigate(EDIT_WEIGHT_ROUTE)
                                "Height" -> navController.navigate(EDIT_HEIGHT_ROUTE)
                                "Body Fat" -> navController.navigate(EDIT_BODY_FAT_ROUTE)
                            }
                        },
                        onNavigateToViewBMIHistory = {
                            navController.navigate(VIEW_BMI_HISTORY_ROUTE)
                        },
                        viewModel = viewModel
                    )
                    1 -> NutritionScreen(navController = navController, viewModel = viewModel)
                    2 -> WorkoutScreen(navController = navController, viewModel = viewModel)
                    3 -> ProgressScreen(navController = navController, viewModel = viewModel)
                    4 -> ProfileScreen(navController = navController, viewModel = viewModel)
                }
            }

            // Plus button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(ADD_ENTRY_ROUTE) },
                    containerColor = Color(0xFF2196F3)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Entry",
                        tint = Color.White
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