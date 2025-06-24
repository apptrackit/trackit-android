package com.ballabotond.trackit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.ballabotond.trackit.data.repository.AuthRepository
import com.ballabotond.trackit.data.repository.MetricsRepository
import com.ballabotond.trackit.data.repository.SyncRepository
import com.ballabotond.trackit.ui.theme.LifeTrackerTheme
import com.ballabotond.trackit.ui.navigation.LifeTrackerNavHost
import com.ballabotond.trackit.ui.viewmodel.AuthViewModel
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.ui.viewmodel.SyncViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create repositories and viewModels
        val metricsRepository = MetricsRepository(this)
        val authRepository = AuthRepository(this)
        val syncRepository = SyncRepository(this, authRepository, metricsRepository)
        val healthViewModel = HealthViewModel(metricsRepository, syncRepository)
        val authViewModel = AuthViewModel(authRepository)
        val syncViewModel = SyncViewModel(syncRepository)
        
        // Ensure all metrics have history entries
        healthViewModel.ensureMetricHistory()

        setContent {
            // Create NavController
            val navController = rememberNavController()
            val authUiState by authViewModel.uiState.collectAsState()

            // Handle navigation based on auth state
            LaunchedEffect(authUiState.isLoggedIn) {
                if (authUiState.isLoggedIn) {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        popUpTo("register") { inclusive = true }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }

            LifeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    LifeTrackerNavHost(
                        navController = navController,
                        authViewModel = authViewModel,
                        viewModel = healthViewModel,
                        syncViewModel = syncViewModel
                    )
                }
            }
        }
    }
}
