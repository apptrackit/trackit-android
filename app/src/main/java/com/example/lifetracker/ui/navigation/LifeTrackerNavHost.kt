package com.example.lifetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lifetracker.data.model.HistoryEntry
import com.example.lifetracker.ui.screens.*
import com.example.lifetracker.ui.viewmodel.HealthViewModel

@Composable
fun LifeTrackerNavHost(
    navController: NavHostController,
    viewModel: HealthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
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
    }
} 
