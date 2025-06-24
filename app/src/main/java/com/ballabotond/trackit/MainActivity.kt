package com.ballabotond.trackit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.ballabotond.trackit.ui.theme.LifeTrackerTheme
import com.ballabotond.trackit.ui.navigation.LifeTrackerNavHost
import com.ballabotond.trackit.ui.viewmodel.HealthViewModel
import com.ballabotond.trackit.data.repository.MetricsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create repository and viewModel
        val repository = MetricsRepository(this)
        val viewModel = HealthViewModel(repository)
        
        // Ensure all metrics have history entries
        viewModel.ensureMetricHistory()

        setContent {
            // Create NavController
            val navController = rememberNavController()

            LifeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    LifeTrackerNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
