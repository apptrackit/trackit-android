package com.ballabotond.trackit.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ballabotond.trackit.data.model.HistoryEntry
import com.ballabotond.trackit.ui.screens.auth.LoginScreen
import com.ballabotond.trackit.ui.screens.auth.RegisterScreen
import com.ballabotond.trackit.ui.screens.dashboard.MainScreen
import com.ballabotond.trackit.ui.screens.health.AddMetricDataScreen
import com.ballabotond.trackit.ui.screens.health.EditMetricDataScreen
import com.ballabotond.trackit.ui.screens.health.EditMetricScreen
import com.ballabotond.trackit.ui.screens.health.ViewBMIHistoryScreen
import com.ballabotond.trackit.ui.screens.health.ViewCalculatedHistoryScreen
import com.ballabotond.trackit.ui.screens.photos.*
import com.ballabotond.trackit.ui.screens.settings.ProfileScreen
import com.ballabotond.trackit.ui.viewmodel.AuthViewModel
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@Composable
fun LifeTrackerNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viewModel: HealthViewModel
) {
    val authUiState by authViewModel.uiState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = if (authUiState.isLoggedIn) "main" else LOGIN_ROUTE
    ) {
        // Auth screens
        composable(LOGIN_ROUTE) {
            LoginScreen(
                uiState = authUiState,
                onLogin = { username, password ->
                    authViewModel.login(username, password)
                },
                onNavigateToRegister = {
                    navController.navigate(REGISTER_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = false }
                    }
                }
            )
        }
        
        composable(REGISTER_ROUTE) {
            RegisterScreen(
                uiState = authUiState,
                onRegister = { username, password, email ->
                    authViewModel.register(username, password, email)
                },
                onNavigateToLogin = {
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(REGISTER_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(navController = navController, viewModel = viewModel)
        }

        composable(EDIT_WEIGHT_ROUTE) {
            EditMetricScreen(
                title = "Weight",
                metricName = "Weight",
                unit = "kg",
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date -> viewModel.updateWeight(value.toString(), date) }
            )
        }

        composable(EDIT_HEIGHT_ROUTE) {
            EditMetricScreen(
                title = "Height",
                metricName = "Height",
                unit = "cm",
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date -> viewModel.updateHeight(value.toString(), date) }
            )
        }

        composable(EDIT_BODY_FAT_ROUTE) {
            EditMetricScreen(
                title = "Body Fat",
                metricName = "Body Fat",
                unit = "%",
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date -> viewModel.updateBodyFat(value.toString(), date) }
            )
        }

        composable(
            route = EDIT_METRIC_ROUTE,
            arguments = listOf(
                navArgument("metricName") { type = NavType.StringType },
                navArgument("unit") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val metricName = backStackEntry.arguments?.getString("metricName") ?: ""
            val unit = backStackEntry.arguments?.getString("unit") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            
            EditMetricScreen(
                title = title,
                metricName = metricName,
                unit = unit,
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date ->
                    when (metricName) {
                        "Weight" -> viewModel.updateWeight(value.toString(), date)
                        "Height" -> viewModel.updateHeight(value.toString(), date)
                        "Body Fat" -> viewModel.updateBodyFat(value.toString(), date)
                        "Waist" -> viewModel.updateWaist(value.toString(), date)
                        "Bicep" -> viewModel.updateBicep(value.toString(), date)
                        "Chest" -> viewModel.updateChest(value.toString(), date)
                        "Thigh" -> viewModel.updateThigh(value.toString(), date)
                        "Shoulder" -> viewModel.updateShoulder(value.toString(), date)
                    }
                }
            )
        }

        composable(VIEW_BMI_HISTORY_ROUTE) {
            ViewBMIHistoryScreen(navController = navController, viewModel = viewModel)
        }

        composable(
            route = ADD_METRIC_DATA_ROUTE,
            arguments = listOf(
                navArgument("metricName") { type = NavType.StringType },
                navArgument("unit") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val metricName = backStackEntry.arguments?.getString("metricName") ?: ""
            val unit = backStackEntry.arguments?.getString("unit") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""

            AddMetricDataScreen(
                title = title,
                metricName = metricName,
                unit = unit,
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date ->
                    when (metricName) {
                        "Weight" -> viewModel.updateWeight(value.toString(), date)
                        "Height" -> viewModel.updateHeight(value.toString(), date)
                        "Body Fat" -> viewModel.updateBodyFat(value.toString(), date)
                        "Waist" -> viewModel.updateWaist(value.toString(), date)
                        "Bicep" -> viewModel.updateBicep(value.toString(), date)
                        "Chest" -> viewModel.updateChest(value.toString(), date)
                        "Thigh" -> viewModel.updateThigh(value.toString(), date)
                        "Shoulder" -> viewModel.updateShoulder(value.toString(), date)
                    }
                }
            )
        }

        composable(
            route = EDIT_METRIC_DATA_ROUTE,
            arguments = listOf(
                navArgument("metricName") { type = NavType.StringType },
                navArgument("unit") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("value") { type = NavType.FloatType },
                navArgument("date") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val metricName = backStackEntry.arguments?.getString("metricName") ?: ""
            val unit = backStackEntry.arguments?.getString("unit") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val value = backStackEntry.arguments?.getFloat("value") ?: 0f
            val date = backStackEntry.arguments?.getLong("date") ?: System.currentTimeMillis()

            EditMetricDataScreen(
                title = title,
                metricName = metricName,
                unit = unit,
                initialValue = value,
                initialDate = date,
                navController = navController,
                viewModel = viewModel,
                onSave = { newValue, newDate ->
                    // Delete the old entry
                    val oldEntry = HistoryEntry(
                        value = value,
                        date = date,
                        metricName = metricName,
                        unit = unit
                    )
                    viewModel.deleteHistoryEntry(metricName, oldEntry)

                    // Add the updated entry
                    when (metricName) {
                        "Weight" -> viewModel.updateWeight(newValue.toString(), newDate)
                        "Height" -> viewModel.updateHeight(newValue.toString(), newDate)
                        "Body Fat" -> viewModel.updateBodyFat(newValue.toString(), newDate)
                        "Waist" -> viewModel.updateWaist(newValue.toString(), newDate)
                        "Bicep" -> viewModel.updateBicep(newValue.toString(), newDate)
                        "Chest" -> viewModel.updateChest(newValue.toString(), newDate)
                        "Thigh" -> viewModel.updateThigh(newValue.toString(), newDate)
                        "Shoulder" -> viewModel.updateShoulder(newValue.toString(), newDate)
                    }
                }
            )
        }
        
        composable(ADD_ENTRY_ROUTE) {
            AddMetricDataScreen(
                title = "Add Entry",
                metricName = "Weight",
                unit = "kg",
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date -> viewModel.updateWeight(value.toString(), date) }
            )
        }

        composable(
            route = "photo_detail/{uri}",
            arguments = listOf(
                navArgument("uri") { 
                    type = NavType.StringType
                    nullable = false 
                }
            )
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri")
            if (uri != null) {
                PhotoDetailScreen(
                    navController = navController,
                    viewModel = viewModel,
                    photoUri = uri
                )
            }
        }
        
        composable(
            route = PHOTO_COMPARE_ROUTE,
            arguments = listOf(
                navArgument("mainUri") { 
                    type = NavType.StringType
                    nullable = false 
                },
                navArgument("compareUri") { 
                    type = NavType.StringType
                    nullable = false 
                }
            )
        ) { backStackEntry ->
            val mainUri = backStackEntry.arguments?.getString("mainUri")
            val compareUri = backStackEntry.arguments?.getString("compareUri")
            if (mainUri != null && compareUri != null) {
                PhotoCompareScreen(
                    navController = navController,
                    viewModel = viewModel,
                    mainPhotoUri = mainUri,
                    comparePhotoUri = compareUri
                )
            }
        }
        
        composable(
            route = PHOTO_CATEGORY_ROUTE,
            arguments = listOf(
                navArgument("uri") { 
                    type = NavType.StringType
                    nullable = false 
                }
            )
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri")
            if (uri != null) {
                PhotoCategoryScreen(
                    navController = navController,
                    viewModel = viewModel,
                    photoUri = uri
                )
            }
        }

        composable(
            route = VIEW_CALCULATED_HISTORY_ROUTE,
            arguments = listOf(
                navArgument("metricName") { type = NavType.StringType },
                navArgument("unit") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val metricName = backStackEntry.arguments?.getString("metricName") ?: ""
            val unit = backStackEntry.arguments?.getString("unit") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            
            ViewCalculatedHistoryScreen(
                navController = navController,
                viewModel = viewModel,
                metricName = metricName,
                unit = unit,
                title = title
            )
        }

        // Add this block to handle the profile route
        composable(
            "profile",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it }, // Slide in from the left
                    animationSpec = tween(durationMillis = 500)
                ) + fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it }, // Slide out to the right
                    animationSpec = tween(durationMillis = 1000)
                ) + fadeOut(animationSpec = tween(1000))
            }
        ) {
            ProfileScreen(
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel
            )
        }
    }
}
