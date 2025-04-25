package com.example.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.lifetracker.data.repository.HealthConnectRepository
import com.example.lifetracker.data.repository.MetricsRepository
import com.example.lifetracker.ui.navigation.LifeTrackerNavHost
import com.example.lifetracker.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            
            // Initialize repositories
            val metricsRepository = MetricsRepository(this)
            val healthConnectRepository = HealthConnectRepository(this)
            
            // Initialize ViewModel with both repositories
            val viewModel = HealthViewModel(metricsRepository, healthConnectRepository)
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LifeTrackerNavHost(navController = navController, viewModel = viewModel)
            }
        }
    }
}
