package com.example.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lifetracker.ui.theme.LifeTrackerTheme
import com.example.lifetracker.ui.navigation.*
import com.example.lifetracker.ui.screens.*
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.data.repository.MetricsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create repository and viewModel
        val repository = MetricsRepository(this)
        val viewModel = HealthViewModel(repository)
        
        // Ensure all metrics have history entries
        viewModel.ensureAllMetricsHaveHistory()

        setContent {
            // Create NavController
            val navController = rememberNavController()

            LifeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = DASHBOARD_ROUTE
                    ) {
                        // Main navigation routes
                        composable(DASHBOARD_ROUTE) {
                            MainScreen(navController, viewModel)
                        }
                        composable(NUTRITION_ROUTE) {
                            MainScreen(navController, viewModel)
                        }
                        composable(WORKOUT_ROUTE) {
                            MainScreen(navController, viewModel)
                        }
                        composable(PROGRESS_ROUTE) {
                            MainScreen(navController, viewModel)
                        }
                        composable(PROFILE_ROUTE) {
                            MainScreen(navController, viewModel)
                        }
                        
                        // Other screens
                        composable(EDIT_WEIGHT_ROUTE) {
                            EditMetricScreen(
                                title = "Weight",
                                metricName = "Weight",
                                unit = "kg",
                                navController = navController,
                                viewModel = viewModel,
                                onSave = { value, date -> viewModel.updateWeight(value, date) }
                            )
                        }
                        composable(EDIT_HEIGHT_ROUTE) {
                            EditMetricScreen(
                                title = "Height",
                                metricName = "Height",
                                unit = "cm",
                                navController = navController,
                                viewModel = viewModel,
                                onSave = { value, date -> viewModel.updateHeight(value, date) }
                            )
                        }
                        composable(EDIT_BODY_FAT_ROUTE) {
                            EditMetricScreen(
                                title = "Body Fat",
                                metricName = "Body Fat",
                                unit = "%",
                                navController = navController,
                                viewModel = viewModel,
                                onSave = { value, date -> viewModel.updateBodyFat(value, date) }
                            )
                        }
                        composable(VIEW_BMI_HISTORY_ROUTE) {
                            ViewBMIHistoryScreen(navController, viewModel)
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
                                        "Weight" -> viewModel.updateWeight(value, date)
                                        "Height" -> viewModel.updateHeight(value, date)
                                        "Body Fat" -> viewModel.updateBodyFat(value, date)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
