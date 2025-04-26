package com.example.lifetracker.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.SensorRequest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataSet

class StepCountRepository(
    private val context: Context,
    private val permissionLauncher: ActivityResultLauncher<android.content.Intent>? = null
) {

    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
        private const val TAG = "StepCountRepository"
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    
    private var latestStepCount = 0
    private var isListening = false
    
    fun hasPermissions(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
        Log.d(TAG, "Checking permissions - has permissions: $hasPermissions")
        return hasPermissions
    }

    fun requestPermissions(activity: Activity, requestCode: Int) {
        try {
            Log.d(TAG, "Requesting permissions, launcher: ${permissionLauncher != null}")
            if (permissionLauncher != null) {
                // Use basic GoogleSignInOptions for the launcher
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
                val intent = GoogleSignIn.getClient(context, gso).signInIntent
                Log.d(TAG, "Launching Google Sign-in intent with ActivityResultLauncher")
                permissionLauncher.launch(intent)
            } else {
                // Use the standard fitness permissions request
                Log.d(TAG, "Launching Google Fit permissions request")
                GoogleSignIn.requestPermissions(
                    activity,
                    requestCode,
                    GoogleSignIn.getLastSignedInAccount(context),
                    fitnessOptions
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permissions", e)
        }
    }

    fun subscribeToStepCountUpdates(callback: (Int) -> Unit) {
        if (!hasPermissions()) {
            Log.e(TAG, "Cannot subscribe to updates: No permissions")
            callback(0)
            return
        }

        if (isListening) {
            Log.d(TAG, "Already listening for step count updates")
            return
        }

        try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            // First get the current step count
            getTodayStepCount { steps -> 
                Log.d(TAG, "Initial step count: $steps")
                callback(steps) 
            }

            // Use Recording API instead of sensor API for better battery performance
            Fitness.getRecordingClient(context, account)
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully subscribed to step count updates")
                    isListening = true
                    
                    // Set up a periodic refresh (every minute)
                    android.os.Handler().postDelayed(object : Runnable {
                        override fun run() {
                            if (isListening) {
                                refreshStepCount(callback)
                                android.os.Handler().postDelayed(this, 60000) // 1 minute
                            }
                        }
                    }, 60000)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to subscribe to step count updates", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to step count updates", e)
            callback(0)
        }
    }

    fun getTodayStepCount(callback: (Int) -> Unit) {
        if (!hasPermissions()) {
            Log.e(TAG, "Cannot get step count: No permissions")
            callback(0)
            return
        }

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        Log.d(TAG, "Getting step count from ${startTime} to ${endTime}")

        try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            // Use the newer way to build the request
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .addOnSuccessListener { response ->
                    var totalSteps = 0
                    
                    // Process all data sets in the response
                    for (dataSet in response.dataSets) {
                        for (dataPoint in dataSet.dataPoints) {
                            for (field in dataPoint.dataType.fields) {
                                val steps = dataPoint.getValue(field).asInt()
                                totalSteps += steps
                                Log.d(TAG, "Field: ${field.name}, Steps: $steps")
                            }
                        }
                    }
                    
                    Log.d(TAG, "Total steps today: $totalSteps")
                    latestStepCount = totalSteps
                    callback(totalSteps)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting step count: ${e.message}", e)
                    callback(latestStepCount) // Use cached value if available
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in step count request: ${e.message}", e)
            callback(latestStepCount) // Use cached value if available
        }
    }
    
    fun refreshStepCount(callback: (Int) -> Unit) {
        Log.d(TAG, "Force refreshing step count data")
        getTodayStepCount(callback)
    }

    fun getWeeklyStepCounts(callback: (List<Pair<Long, Int>>) -> Unit) {
        if (!hasPermissions()) {
            Log.e(TAG, "Cannot get weekly step counts: No permissions")
            callback(emptyList())
            return
        }

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis

        try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            // Use daily step count for the past week
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
                
            Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .addOnSuccessListener { response ->
                    // Process the data by day
                    val stepsByDay = mutableMapOf<Long, Int>()
                    
                    for (dataSet in response.dataSets) {
                        for (dataPoint in dataSet.dataPoints) {
                            // Get the day timestamp (midnight of that day)
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            val dayTimestamp = cal.timeInMillis
                            
                            // Add steps to that day
                            val currentSteps = stepsByDay.getOrDefault(dayTimestamp, 0)
                            val steps = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                            stepsByDay[dayTimestamp] = currentSteps + steps
                        }
                    }
                    
                    // Convert to list of pairs and sort by date
                    val stepCounts = stepsByDay.map { (timestamp, steps) -> 
                        Pair(timestamp, steps)
                    }.sortedBy { it.first }
                    
                    Log.d(TAG, "Weekly step counts: ${stepCounts.size} days")
                    callback(stepCounts)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting weekly step counts: ${e.message}", e)
                    callback(emptyList())
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in weekly step count request: ${e.message}", e)
            callback(emptyList())
        }
    }
}
