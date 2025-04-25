package com.example.lifetracker.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class HealthConnectRepository(private val context: Context) {
    
    private val TAG = "HealthConnectRepository"
    
    // Initialize client as a private property
    private var healthConnectClient: HealthConnectClient? = null
    
    // Step count state
    private val _stepCount = MutableStateFlow(0L)
    val stepCount: StateFlow<Long> = _stepCount.asStateFlow()
    
    // Define permissions needed
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )
    
    init {
        try {
            if (isHealthConnectAvailable()) {
                healthConnectClient = HealthConnectClient.getOrCreate(context)
                Log.d(TAG, "HealthConnectClient initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize HealthConnectClient", e)
            healthConnectClient = null
        }
    }
    
    // Check if Health Connect package is installed
    private fun isHealthConnectAvailable(): Boolean {
        val healthConnectPackageName = "com.google.android.apps.healthdata"
        return try {
            context.packageManager.getPackageInfo(healthConnectPackageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    // Check if Health Connect is available on the device
    fun hasHealthConnectCapability(): Boolean {
        val available = healthConnectClient != null && isHealthConnectAvailable()
        Log.d(TAG, "Health Connect available: $available")
        return available
    }
    
    // Create intent for requesting Health Connect permissions
    fun createPermissionRequestIntent(): Intent? {
        if (healthConnectClient == null) {
            Log.e(TAG, "HealthConnectClient is null - can't request permissions")
            return getHealthConnectAppIntent()
        }
        
        return try {
            // Skip the API call and create the intent directly - works across all HC versions
            val manualIntent = Intent("androidx.health.ACTION_HEALTH_CONNECT_PERMISSIONS").apply {
                putExtra("androidx.health.EXTRA_PERMISSIONS", permissions.map { it.toString() }.toTypedArray())
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            Log.d(TAG, "Created permission intent manually: $manualIntent")
            manualIntent
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create manual permission intent", e)
            getHealthConnectAppIntent()
        }
    }
    
    // Get intent to open Health Connect app directly
    fun getHealthConnectAppIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            // Try a more direct approach with the permissions page
            data = Uri.parse("healthconnect://permissions")
            setPackage("com.google.android.apps.healthdata")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            Log.d(TAG, "Created Health Connect app intent: $it")
        }
    }

    // Get intent to install Health Connect
    fun getHealthConnectInstallIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    // Explicitly request permissions - use this as an alternative approach
    suspend fun requestPermissions() {
        healthConnectClient?.let { client ->
            try {
                // FIXED: Using intent-based approach instead of direct API call
                // The Health Connect API doesn't have a direct requestPermission method in this version
                Log.d(TAG, "Attempting to request permissions via intent")
                val intent = createPermissionRequestIntent()
                if (intent != null) {
                    // We need to handle this intent in the UI layer, can't launch directly from repository
                    Log.d(TAG, "Created permission intent, UI should handle launching it")
                    // Just log here, the actual launching is handled in the ViewModel/UI
                } else {
                    Log.e(TAG, "Failed to create permission intent")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request permissions", e)
            }
        }
    }
    
    // Check if permissions are granted
    suspend fun checkPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        
        return try {
            val grantedPermissions = client.permissionController.getGrantedPermissions()
            val requiredPermissions = permissions
            val hasAllPermissions = grantedPermissions.containsAll(requiredPermissions)
            
            // Print more detailed debugging info 
            Log.d(TAG, "Checking permissions:")
            Log.d(TAG, "- Required: ${requiredPermissions.map { it.toString() }}")
            Log.d(TAG, "- Granted: ${grantedPermissions.map { it.toString() }}")
            Log.d(TAG, "- Has all required: $hasAllPermissions")
            
            if (hasAllPermissions) {
                // Try to read step data as a verification
                try {
                    readTodayStepData()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read step data after permissions check", e)
                }
            }
            
            hasAllPermissions
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            false
        }
    }
    
    // Get today's step count
    suspend fun readTodayStepData() {
        val client = healthConnectClient
        if (client == null) {
            Log.e(TAG, "HealthConnectClient is null when reading step data")
            return
        }
        
        try {
            Log.d(TAG, "Reading step data...")
            
            // Define today's time range
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault())
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            
            // Create time range filter
            val timeRangeFilter = TimeRangeFilter.between(
                startOfDay.toInstant(), 
                endOfDay.toInstant()
            )
            
            // Make the request
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )
            
            // Calculate total steps
            val totalSteps = response.records.sumOf { it.count }
            _stepCount.value = totalSteps
            
            Log.d(TAG, "Step data: $totalSteps steps from ${response.records.size} records")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read step data", e)
            e.printStackTrace()
        }
    }

    // Get step data for a specific date range
    suspend fun readStepDataForRange(startTime: ZonedDateTime, endTime: ZonedDateTime): Long {
        val client = healthConnectClient ?: return 0
        
        try {
            // Create time range filter
            val timeRangeFilter = TimeRangeFilter.between(
                startTime.toInstant(),
                endTime.toInstant()
            )
            
            // Make the request
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )
            
            // Return total steps
            return response.records.sumOf { it.count }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read step data for range", e)
            return 0
        }
    }
}


