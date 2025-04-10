package com.example.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.lifetracker.ui.theme.LifeTrackerTheme
import com.example.lifetracker.ui.navigation.LifeTrackerNavHost
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.data.repository.MetricsRepository

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
