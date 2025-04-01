package com.example.lifetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifetracker.data.repository.MetricsRepository
import com.example.lifetracker.ui.screens.DashboardScreen
import com.example.lifetracker.ui.screens.EditMetricScreen
import com.example.lifetracker.ui.viewmodel.HealthViewModel

@Composable
fun LifeTrackerNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { MetricsRepository(context) }
    val viewModel = remember { HealthViewModel(repository) }

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(navController, viewModel)
        }
        composable("edit_weight") {
            EditMetricScreen(
                title = "Edit Weight",
                metricName = "Weight",
                unit = "kg",
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date ->
                    val floatValue = value.toFloatOrNull() ?: 0f
                    viewModel.updateWeight(value, date)
                    viewModel.saveMetricHistory("Weight", floatValue, "kg", date)
                    navController.popBackStack()
                }
            )
        }

        composable("edit_height") {
            EditMetricScreen(
                title = "Edit Height",
                metricName = "Height",
                unit = "cm",
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date ->
                    val floatValue = value.toFloatOrNull() ?: 0f
                    viewModel.updateHeight(value, date)
                    viewModel.saveMetricHistory("Height", floatValue, "cm", date)
                    navController.popBackStack()
                }
            )
        }

        composable("edit_bodyfat") {
            EditMetricScreen(
                title = "Edit Body Fat",
                metricName = "Body Fat",
                unit = "%",
                navController = navController,
                viewModel = viewModel,
                onSave = { value, date ->
                    val floatValue = value.toFloatOrNull() ?: 0f
                    viewModel.updateBodyFat(value, date)
                    viewModel.saveMetricHistory("Body Fat", floatValue, "%", date)
                    navController.popBackStack()
                }
            )
        }

    }
}
