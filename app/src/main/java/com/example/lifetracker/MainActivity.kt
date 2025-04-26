package com.example.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.lifetracker.data.repository.MetricsRepository
import com.example.lifetracker.data.repository.StepCountRepository
import com.example.lifetracker.ui.navigation.LifeTrackerNavHost
import com.example.lifetracker.ui.theme.LifeTrackerTheme
import com.example.lifetracker.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val metricsRepository = MetricsRepository(this)
                    val stepCountRepository = StepCountRepository(this)
                    val viewModel = HealthViewModel(metricsRepository, stepCountRepository)
                    
                    LifeTrackerNavHost(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            StepCountRepository.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                if (resultCode == ComponentActivity.RESULT_OK) {
                    // Permissions granted
                }
            }
        }
    }
}
