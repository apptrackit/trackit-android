package com.example.lifetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lifetracker.ui.navigation.*
import com.example.lifetracker.ui.theme.FontAwesomeIcons
import com.example.lifetracker.ui.theme.FontAwesomeIcon
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.google.accompanist.pager.*
import com.guru.fontawesomecomposelib.FaIcons
import com.guru.fontawesomecomposelib.FaIconType
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
        NavigationItem(DASHBOARD_ROUTE, FontAwesomeIcons.Home, "Dashboard"),
        NavigationItem(NUTRITION_ROUTE, FontAwesomeIcons.List, "Nutrition"),
        NavigationItem(PHOTOS_ROUTE, FontAwesomeIcons.Images, "Photos"),
        NavigationItem(PROGRESS_ROUTE, FontAwesomeIcons.ChartLine, "Progress"),
        NavigationItem(PROFILE_ROUTE, FontAwesomeIcons.User, "Profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1A1A),
                tonalElevation = 8.dp,
                modifier = Modifier.height(64.dp)  // Increased height
            ) {
                pages.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            FontAwesomeIcon(
                                icon = item.icon, 
                                tint = if (pagerState.currentPage == index) Color(0xFF2196F3) else Color.White,
                                modifier = Modifier.size(20.dp)  // Adjusted icon size
                            ) 
                        },
                        label = { 
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                letterSpacing = 0.3.sp,
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
                            unselectedIconColor = Color.White.copy(alpha = 0.8f),
                            unselectedTextColor = Color.White.copy(alpha = 0.8f),
                            indicatorColor = Color(0xFF1A1A1A)
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
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
                        viewModel = viewModel,
                        navController = navController
                    )
                    1 -> NutritionScreen(navController = navController, viewModel = viewModel)
                    2 -> PhotosScreen(navController = navController, viewModel = viewModel)
                    3 -> ProgressScreen(
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
                        viewModel = viewModel,
                        navController = navController
                    )
                    4 -> ProfileScreen(navController = navController, viewModel = viewModel)
                }
            }

        }
    }
}

private data class NavigationItem(
    val route: String,
    val icon: FaIconType.SolidIcon,
    val label: String
)