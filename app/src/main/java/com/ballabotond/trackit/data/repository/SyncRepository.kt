package com.ballabotond.trackit.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ballabotond.trackit.data.api.MetricsApiService
import com.ballabotond.trackit.data.model.*
import com.ballabotond.trackit.data.network.NetworkModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

class SyncRepository(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val metricsRepository: MetricsRepository
) {
    private val metricsApi: MetricsApiService = NetworkModule.metricsApiService
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private val LAST_SYNC_KEY = longPreferencesKey("last_sync_timestamp")
        private val PENDING_SYNC_ENTRIES_KEY = stringPreferencesKey("pending_sync_entries")
        private val METRIC_TYPE_MAPPINGS_KEY = stringPreferencesKey("metric_type_mappings")
        
        // Default metric type mappings - will be updated from server
        // Only include base measurements, not calculated metrics
        private val DEFAULT_METRIC_MAPPINGS = mapOf(
            "Weight" to 1,
            "Height" to 2,
            "Body Fat" to 3,
            "Chest" to 4,
            "Waist" to 5,
            "Bicep" to 6,
            "Thigh" to 7,
            "Shoulder" to 8,
            "Glutes" to 9,
            "Calf" to 10,
            "Neck" to 11,
            "Forearm" to 12
        )
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    suspend fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    suspend fun getLastSyncTimestamp(): Long {
        val preferences = context.syncDataStore.data.first()
        return preferences[LAST_SYNC_KEY] ?: 0L
    }
    
    private suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.syncDataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = timestamp
        }
    }
    
    private suspend fun getMetricTypeId(metricName: String): Int? {
        val preferences = context.syncDataStore.data.first()
        val mappingsJson = preferences[METRIC_TYPE_MAPPINGS_KEY]
        
        val mappings = if (mappingsJson != null) {
            try {
                json.decodeFromString<Map<String, Int>>(mappingsJson)
            } catch (e: Exception) {
                DEFAULT_METRIC_MAPPINGS
            }
        } else {
            // Save default mappings
            saveMetricMappings(DEFAULT_METRIC_MAPPINGS)
            DEFAULT_METRIC_MAPPINGS
        }
        
        return mappings[metricName]
    }
    
    private suspend fun saveMetricMappings(mappings: Map<String, Int>) {
        context.syncDataStore.edit { preferences ->
            preferences[METRIC_TYPE_MAPPINGS_KEY] = json.encodeToString(mappings)
        }
    }
    
    suspend fun syncAllData(): Result<SyncState> {
        if (!isOnline()) {
            return Result.failure(Exception("No internet connection"))
        }
        
        if (!authRepository.isLoggedIn()) {
            return Result.failure(Exception("User not logged in"))
        }
        
        return try {
            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                return Result.failure(Exception("No access token"))
            }
            
            // First, sync metric types from server
            syncMetricTypes(accessToken)
            
            // Download server data
            downloadServerData(accessToken)
            
            // Upload pending local changes
            val pendingEntries = getPendingEntries()
            var successCount = 0
            var failCount = 0
            
            // Upload pending entries
            for (entry in pendingEntries) {
                when (entry.syncStatus) {
                    SyncStatus.PENDING, SyncStatus.FAILED -> {
                        val result = uploadEntry(accessToken, entry)
                        if (result.isSuccess) {
                            successCount++
                            // Update entry with server ID and mark as synced
                            updateEntryAfterSync(entry, result.getOrNull()?.entryId, SyncStatus.SYNCED)
                        } else {
                            failCount++
                            updateEntryAfterSync(entry, null, SyncStatus.FAILED)
                        }
                    }
                    SyncStatus.DELETED_LOCALLY -> {
                        if (entry.serverId != null) {
                            val result = deleteEntryOnServer(accessToken, entry.serverId)
                            if (result.isSuccess) {
                                removeEntryFromPending(entry)
                            } else {
                                failCount++
                            }
                        }
                    }
                    else -> { /* Already synced or syncing */ }
                }
            }
            
            // Update last sync timestamp
            setLastSyncTimestamp(System.currentTimeMillis())
            
            val finalPendingEntries = getPendingEntries()
            Result.success(
                SyncState(
                    lastSyncTimestamp = System.currentTimeMillis(),
                    isOnline = true,
                    isSyncing = false,
                    pendingUploads = finalPendingEntries.count { it.syncStatus == SyncStatus.PENDING },
                    failedUploads = finalPendingEntries.count { it.syncStatus == SyncStatus.FAILED }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncMetricTypes(accessToken: String) {
        try {
            val response = metricsApi.getMetricTypes("Bearer $accessToken")
            if (response.isSuccessful && response.body() != null) {
                val typesResponse = response.body()!!
                if (typesResponse.success) {
                    // Update local metric type mappings
                    val mappings = typesResponse.types.associate { type ->
                        type.name to type.id
                    }
                    saveMetricMappings(mappings)
                }
            }
        } catch (e: Exception) {
            // Log but don't fail sync if types can't be retrieved
            println("Failed to sync metric types: ${e.message}")
        }
    }
    
    private suspend fun downloadServerData(accessToken: String) {
        try {
            val response = metricsApi.getMetricEntries("Bearer $accessToken", limit = 1000)
            if (response.isSuccessful && response.body() != null) {
                val metricsResponse = response.body()!!
                if (metricsResponse.success) {
                    // Convert server entries to local format and save
                    for (serverEntry in metricsResponse.entries) {
                        convertAndSaveServerEntry(serverEntry)
                    }
                }
            }
        } catch (e: Exception) {
            // Log but don't fail sync if download fails
            println("Failed to download server data: ${e.message}")
        }
    }
    
    private suspend fun convertAndSaveServerEntry(serverEntry: ServerMetricEntry) {
        try {
            // Find local metric name for server type ID
            val localMetricName = getLocalMetricName(serverEntry.metric_type_id)
            if (localMetricName != null) {
                // Parse date string to timestamp
                val date = try {
                    dateFormat.parse(serverEntry.date)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
                
                // Check if we already have this entry locally
                val localHistory = metricsRepository.getMetricHistory(localMetricName, getUnitForMetric(localMetricName))
                val existingEntry = localHistory.find { entry ->
                    abs(entry.date - date) < 60000 && // Within 1 minute
                    abs(entry.value - serverEntry.value) < 0.01f // Same value
                }
                
                if (existingEntry == null) {
                    // Save new entry from server
                    metricsRepository.saveMetricHistory(
                        metricName = localMetricName,
                        value = serverEntry.value,
                        unit = getUnitForMetric(localMetricName),
                        date = date
                    )
                    
                    // Mark as synced in pending list
                    val syncEntry = SyncMetricEntry(
                        localId = "${localMetricName}_$date",
                        serverId = serverEntry.id,
                        metricTypeId = serverEntry.metric_type_id,
                        value = serverEntry.value,
                        date = serverEntry.date,
                        isAppleHealth = serverEntry.is_apple_health,
                        syncStatus = SyncStatus.SYNCED
                    )
                    addToPendingEntries(syncEntry)
                }
            }
        } catch (e: Exception) {
            println("Failed to convert server entry: ${e.message}")
        }
    }
    
    private suspend fun getLocalMetricName(serverTypeId: Int): String? {
        val preferences = context.syncDataStore.data.first()
        val mappingsJson = preferences[METRIC_TYPE_MAPPINGS_KEY] ?: return null
        
        val mappings = try {
            json.decodeFromString<Map<String, Int>>(mappingsJson)
        } catch (e: Exception) {
            return null
        }
        
        return mappings.entries.find { it.value == serverTypeId }?.key
    }
    
    private fun getUnitForMetric(metricName: String): String {
        return when (metricName) {
            "Weight" -> "kg"
            "Height" -> "cm"
            "Body Fat" -> "%"
            "Chest", "Waist", "Bicep", "Thigh", "Shoulder", "Glutes", "Calf", "Neck", "Forearm" -> "cm"
            else -> ""
        }
    }
    
    private suspend fun uploadEntry(accessToken: String, entry: SyncMetricEntry): Result<MetricResponse> {
        return try {
            val request = CreateMetricRequest(
                metric_type_id = entry.metricTypeId,
                value = entry.value,
                date = entry.date,
                is_apple_health = entry.isAppleHealth
            )
            
            val response = metricsApi.createMetricEntry("Bearer $accessToken", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun deleteEntryOnServer(accessToken: String, serverId: String): Result<MetricResponse> {
        return try {
            val response = metricsApi.deleteMetricEntry("Bearer $accessToken", serverId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Delete failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun queueForSync(historyEntry: HistoryEntry) {
        val metricTypeId = getMetricTypeId(historyEntry.metricName)
        if (metricTypeId == null) {
            // Skip metrics that don't have server mappings
            return
        }
        
        val syncEntry = SyncMetricEntry(
            localId = "${historyEntry.metricName}_${historyEntry.date}",
            metricTypeId = metricTypeId,
            value = historyEntry.value,
            date = dateFormat.format(Date(historyEntry.date)),
            syncStatus = SyncStatus.PENDING
        )
        
        addToPendingEntries(syncEntry)
    }
    
    suspend fun queueForDeletion(historyEntry: HistoryEntry) {
        val metricTypeId = getMetricTypeId(historyEntry.metricName)
        if (metricTypeId == null) {
            return
        }
        
        // Find the entry in pending list and mark for deletion
        val pendingEntries = getPendingEntries().toMutableList()
        val existingEntry = pendingEntries.find { 
            it.localId == "${historyEntry.metricName}_${historyEntry.date}" 
        }
        
        if (existingEntry != null) {
            val updatedEntry = existingEntry.copy(syncStatus = SyncStatus.DELETED_LOCALLY)
            pendingEntries.removeAll { it.localId == existingEntry.localId }
            pendingEntries.add(updatedEntry)
            savePendingEntries(pendingEntries)
        }
    }
    
    private suspend fun getPendingEntries(): List<SyncMetricEntry> {
        val preferences = context.syncDataStore.data.first()
        val entriesJson = preferences[PENDING_SYNC_ENTRIES_KEY] ?: return emptyList()
        
        return try {
            json.decodeFromString<List<SyncMetricEntry>>(entriesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun addToPendingEntries(entry: SyncMetricEntry) {
        val currentEntries = getPendingEntries().toMutableList()
        
        // Remove existing entry with same localId if it exists
        currentEntries.removeAll { it.localId == entry.localId }
        currentEntries.add(entry)
        
        savePendingEntries(currentEntries)
    }
    
    private suspend fun savePendingEntries(entries: List<SyncMetricEntry>) {
        context.syncDataStore.edit { preferences ->
            preferences[PENDING_SYNC_ENTRIES_KEY] = json.encodeToString(entries)
        }
    }
    
    private suspend fun updateEntryAfterSync(entry: SyncMetricEntry, serverId: String?, status: SyncStatus) {
        val currentEntries = getPendingEntries().toMutableList()
        val index = currentEntries.indexOfFirst { it.localId == entry.localId }
        
        if (index != -1) {
            currentEntries[index] = entry.copy(
                serverId = serverId ?: entry.serverId,
                syncStatus = status,
                lastSyncAttempt = System.currentTimeMillis()
            )
            savePendingEntries(currentEntries)
        }
    }
    
    private suspend fun removeEntryFromPending(entry: SyncMetricEntry) {
        val currentEntries = getPendingEntries().toMutableList()
        currentEntries.removeAll { it.localId == entry.localId }
        savePendingEntries(currentEntries)
    }
    
    fun getSyncStateFlow(): Flow<SyncState> {
        return context.syncDataStore.data.map { preferences ->
            val lastSync = preferences[LAST_SYNC_KEY] ?: 0L
            val entriesJson = preferences[PENDING_SYNC_ENTRIES_KEY] ?: ""
            
            val pendingEntries = try {
                if (entriesJson.isNotEmpty()) {
                    json.decodeFromString<List<SyncMetricEntry>>(entriesJson)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
            
            SyncState(
                lastSyncTimestamp = lastSync,
                isOnline = false, // Will be updated by checking network state
                isSyncing = false,
                pendingUploads = pendingEntries.count { it.syncStatus == SyncStatus.PENDING },
                failedUploads = pendingEntries.count { it.syncStatus == SyncStatus.FAILED }
            )
        }
    }
}
