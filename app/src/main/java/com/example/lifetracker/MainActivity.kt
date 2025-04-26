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
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private lateinit var viewModel: HealthViewModel
    private lateinit var stepCountRepository: StepCountRepository
    
    // Define the fitness options here to match what's in the repository
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    
    // Register the activity result launcher for Google Fit permissions
    private val googleFitPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Fit permission result: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "Google Fit permissions granted")
            viewModel.handleGoogleFitPermissionResult(true)
        } else {
            Log.d(TAG, "Google Fit permissions denied. Result code: ${result.resultCode}")
            viewModel.handleGoogleFitPermissionResult(false)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "MainActivity onCreate")
        
        val metricsRepository = MetricsRepository(this)
        stepCountRepository = StepCountRepository(this, googleFitPermissionLauncher)
        viewModel = HealthViewModel(metricsRepository, stepCountRepository)
        
        setContent {
            LifeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    LifeTrackerNavHost(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity onResume")
        
        // Check if we have permissions and refresh data if we do
        if (viewModel.hasGoogleFitPermissions()) {
            Log.d(TAG, "Have Google Fit permissions, refreshing data")
            viewModel.subscribeToStepCountUpdates()
            viewModel.refreshTodayStepCount()
        }
    }
}
