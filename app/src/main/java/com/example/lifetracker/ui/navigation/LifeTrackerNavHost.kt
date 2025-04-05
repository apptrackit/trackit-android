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

// Existing routes
const val DASHBOARD_ROUTE = "dashboard"
const val EDIT_WEIGHT_ROUTE = "edit_weight"
const val EDIT_HEIGHT_ROUTE = "edit_height"
const val EDIT_BODY_FAT_ROUTE = "edit_body_fat"
const val ADD_METRIC_DATA_ROUTE = "add_metric_data/{metricName}/{unit}/{title}"

// New route for editing existing entries
const val EDIT_METRIC_DATA_ROUTE = "edit_metric_data/{metricName}/{unit}/{title}/{value}/{date}"

@Composable
fun LifeTrackerNavHost(
    navController: NavHostController,
    viewModel: HealthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = DASHBOARD_ROUTE
    ) {
        composable(DASHBOARD_ROUTE) {
            DashboardScreen(navController, viewModel)
        }

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

        // Route for adding new metric data
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

        // New route for editing existing metric data
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
                        "Weight" -> viewModel.updateWeight(newValue, newDate)
                        "Height" -> viewModel.updateHeight(newValue, newDate)
                        "Body Fat" -> viewModel.updateBodyFat(newValue, newDate)
                    }
                }
            )
        }
    }
}
