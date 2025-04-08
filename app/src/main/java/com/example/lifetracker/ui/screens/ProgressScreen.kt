package com.example.lifetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.lifetracker.ui.viewmodel.HealthViewModel

@Composable
fun ProgressScreen(
    onNavigateToEditMetric: (String) -> Unit,
    onNavigateToViewBMIHistory: () -> Unit,
    viewModel: HealthViewModel,
    navController: NavController
) {
    // Copy the entire content of DashboardScreen here
    // This includes all UI components, state management, and functionality
    DashboardScreen(
        onNavigateToEditMetric = onNavigateToEditMetric,
        onNavigateToViewBMIHistory = onNavigateToViewBMIHistory,
        viewModel = viewModel,
        navController = navController
    )
}