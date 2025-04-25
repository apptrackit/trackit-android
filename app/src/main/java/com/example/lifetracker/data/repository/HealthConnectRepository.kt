package com.example.lifetracker.data.repository

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
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
    
    private val client by lazy { HealthConnectClient.getOrCreate(context) }
    
    private val _stepCount = MutableStateFlow(0L)
    val stepCount: StateFlow<Long> = _stepCount.asStateFlow()
    
    // Define permissions needed
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )
    
    // Check if Health Connect is available on the device
    fun hasHealthConnectCapability(): Boolean {
        val healthConnectPackageName = "com.google.android.apps.healthdata"
        return try {
            val packageInfo = context.packageManager.getPackageInfo(healthConnectPackageName, 0)
            packageInfo != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    // Get permission request contract
    fun getPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }
    
    // Check if permissions are granted
    suspend fun checkPermissions(): Boolean {
        val grantedPermissions = client.permissionController.getGrantedPermissions()
        return grantedPermissions.containsAll(permissions)
    }
    
    // Get today's step count
    suspend fun readTodayStepData() {
        try {
            // Define the time range for today
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
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }
    }

    // Get step data for a specific date range
    suspend fun readStepDataForRange(startTime: ZonedDateTime, endTime: ZonedDateTime): Long {
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
            e.printStackTrace()
            return 0
        }
    }
}

