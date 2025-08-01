package com.ballabotond.trackit.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ballabotond.trackit.data.api.MetricsApiService
import com.ballabotond.trackit.data.api.ImagesApiService
import com.ballabotond.trackit.data.model.*
import com.ballabotond.trackit.data.network.NetworkModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.abs
import com.ballabotond.trackit.utils.toIso8601Utc

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

class SyncRepository(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val metricsRepository: MetricsRepository,
    private val imagesApiService: ImagesApiService
) {
    private val metricsApi: MetricsApiService = NetworkModule.metricsApiService
    private val imagesApi: ImagesApiService = imagesApiService
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private val LAST_SYNC_KEY = longPreferencesKey("last_sync_timestamp")
        private val PENDING_SYNC_ENTRIES_KEY = stringPreferencesKey("pending_sync_entries")
        private val METRIC_TYPE_MAPPINGS_KEY = stringPreferencesKey("metric_type_mappings")
        private val PENDING_IMAGE_ENTRIES_KEY = stringPreferencesKey("pending_image_sync_entries")
        
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
    
    private val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
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
            
            // Download server images
            downloadServerImages(accessToken)
            
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
            
            // --- IMAGE SYNC ---
            val pendingImages = getPendingImageEntries()
            println("SyncRepository: Found ${pendingImages.size} pending images to sync")
            for (imageEntry in pendingImages) {
                println("SyncRepository: Processing image entry - localId: ${imageEntry.localId}, status: ${imageEntry.syncStatus}, filePath: ${imageEntry.filePath}")
                when (imageEntry.syncStatus) {
                    SyncStatus.PENDING, SyncStatus.FAILED -> {
                        println("SyncRepository: Attempting to upload image: ${imageEntry.filePath}")
                        val result = uploadImageEntry(accessToken, imageEntry)
                        if (result.isSuccess) {
                            println("SyncRepository: Image upload successful")
                            val serverId = result.getOrNull()
                            updateImageEntryAfterUpload(imageEntry, serverId, SyncStatus.SYNCED)
                        } else {
                            println("SyncRepository: Image upload failed: ${result.exceptionOrNull()?.message}")
                            updateImageEntryAfterSync(imageEntry, SyncStatus.FAILED)
                            failCount++
                        }
                    }
                    SyncStatus.DELETED_LOCALLY -> {
                        println("SyncRepository: Processing DELETED_LOCALLY image - serverId: ${imageEntry.serverId}")
                        if (imageEntry.serverId != null) {
                            println("SyncRepository: Image has serverId ${imageEntry.serverId}, attempting server deletion")
                            val result = deleteImageOnServer(accessToken, imageEntry.serverId)
                            if (result.isSuccess) {
                                println("SyncRepository: Server deletion successful for image ${imageEntry.serverId}")
                                removeImageEntryFromPending(imageEntry)
                                println("SyncRepository: Removed image entry from pending list")
                            } else {
                                println("SyncRepository: Server deletion failed for image ${imageEntry.serverId}: ${result.exceptionOrNull()?.message}")
                                failCount++
                            }
                        } else {
                            // No server ID means it was never uploaded, just remove from pending
                            println("SyncRepository: Image has no serverId (never uploaded), removing from pending")
                            removeImageEntryFromPending(imageEntry)
                            println("SyncRepository: Removed local-only image from pending list")
                        }
                    }
                    else -> {
                        println("SyncRepository: Skipping image entry with status: ${imageEntry.syncStatus}")
                    }
                }
            }
            
            // Update last sync timestamp
            setLastSyncTimestamp(System.currentTimeMillis())
            
            val finalPendingEntries = getPendingEntries()
            val finalPendingImages = getPendingImageEntries()
            Result.success(
                SyncState(
                    lastSyncTimestamp = System.currentTimeMillis(),
                    isOnline = true,
                    isSyncing = false,
                    pendingUploads = finalPendingEntries.count { it.syncStatus == SyncStatus.PENDING } + finalPendingImages.count { it.syncStatus == SyncStatus.PENDING },
                    failedUploads = finalPendingEntries.count { it.syncStatus == SyncStatus.FAILED } + finalPendingImages.count { it.syncStatus == SyncStatus.FAILED }
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
    
    private suspend fun downloadServerImages(accessToken: String) {
        try {
            println("SyncRepository: Downloading server images...")
            val response = imagesApi.getImages(limit = 1000)
            println("SyncRepository: Images API response - HTTP ${response.code()}, success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val imagesResponse = response.body()!!
                if (imagesResponse.success) {
                    println("SyncRepository: Found ${imagesResponse.images.size} images on server")
                    // Convert server entries to local format and save
                    for (serverImage in imagesResponse.images) {
                        convertAndSaveServerImage(serverImage)
                    }
                } else {
                    println("SyncRepository: Server returned error for images request")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("SyncRepository: Failed to download images - HTTP ${response.code()}")
                println("SyncRepository: Error response body: $errorBody")
                println("SyncRepository: Response headers: ${response.headers()}")
            }
        } catch (e: Exception) {
            // Log but don't fail sync if download fails
            println("SyncRepository: Exception during server images download: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun convertAndSaveServerImage(serverImage: ServerImageEntry) {
        try {
            println("SyncRepository: Processing server image - ID: ${serverImage.id}, filename: ${serverImage.filename}")
            
            // Parse date string to timestamp
            val date = try {
                dateFormat.parse(serverImage.date)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
            
            // Check if this image is marked for deletion locally
            val pendingImages = getPendingImageEntries()
            val markedForDeletion = pendingImages.any { pendingImage ->
                // Check by server ID if available
                if (pendingImage.serverId == serverImage.id && pendingImage.syncStatus == SyncStatus.DELETED_LOCALLY) {
                    return@any true
                }
                false
            }
            
            if (markedForDeletion) {
                println("SyncRepository: Skipping server image ${serverImage.id} - marked for deletion locally")
                return
            }
            
            // Check if this image is already in our pending list (to avoid duplicates)
            val alreadyInPending = pendingImages.any { pendingImage ->
                pendingImage.serverId == serverImage.id ||
                (pendingImage.imageTypeId == serverImage.image_type_id && 
                 abs((try { dateFormat.parse(pendingImage.date)?.time ?: 0L } catch (e: Exception) { 0L }) - date) < 60000)
            }
            
            if (!alreadyInPending) {
                println("SyncRepository: Downloading and saving server image to local storage")
                
                // Download the actual image file
                try {
                    val downloadResponse = imagesApi.downloadImage(serverImage.id)
                    if (downloadResponse.isSuccessful && downloadResponse.body() != null) {
                        val imageBytes = downloadResponse.body()!!.bytes()
                        
                        // Save to local photo directory
                        val photosDir = ensureDirectoryExists(context, "photos")
                        val metadataDir = ensureDirectoryExists(context, "photo_metadata")
                        
                        val fileName = "IMG_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date(date))}_server_${serverImage.id}.jpg"
                        val photoFile = java.io.File(photosDir, fileName)
                        
                        // Write image file
                        photoFile.writeBytes(imageBytes)
                        photoFile.setLastModified(date)
                        
                        // Create metadata file with correct category and server ID
                        val category = mapImageTypeIdToCategory(serverImage.image_type_id)
                        savePhotoMetadata(metadataDir, fileName, category, date, serverImage.id)
                        
                        println("SyncRepository: Server image ${serverImage.id} downloaded and saved as $fileName")
                        
                        // Track as synced in sync state
                        val syncEntry = SyncImageEntry(
                            localId = photoFile.absolutePath,
                            serverId = serverImage.id, // Already a String
                            filePath = photoFile.absolutePath,
                            imageTypeId = serverImage.image_type_id,
                            date = serverImage.date,
                            syncStatus = SyncStatus.SYNCED
                        )
                        addToPendingImageEntries(syncEntry)
                        
                    } else {
                        println("SyncRepository: Failed to download image ${serverImage.id} - HTTP ${downloadResponse.code()}")
                    }
                } catch (e: Exception) {
                    println("SyncRepository: Exception downloading image ${serverImage.id}: ${e.message}")
                }
            } else {
                println("SyncRepository: Server image ${serverImage.id} already exists in pending list")
            }
        } catch (e: Exception) {
            println("SyncRepository: Failed to convert server image entry: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun ensureDirectoryExists(context: Context, dirName: String): java.io.File {
        val dir = java.io.File(context.filesDir, dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    private fun mapImageTypeIdToCategory(imageTypeId: Int): String {
        return when (imageTypeId) {
            1 -> "FRONT"
            2 -> "BACK" 
            3 -> "SIDE"
            4 -> "BICEPS"
            5 -> "CHEST"
            6 -> "LEGS"
            7 -> "FULL_BODY"
            8 -> "OTHER"
            else -> "OTHER"
        }
    }
    
    private fun savePhotoMetadata(metadataDir: java.io.File, fileName: String, category: String, timestamp: Long, serverId: String? = null) {
        val metadataFile = java.io.File(metadataDir, "${fileName}.metadata")
        val metadata = buildString {
            appendLine("category=$category")
            appendLine("timestamp=$timestamp")
            appendLine("source=server")
            serverId?.let { appendLine("server_id=$it") }
        }.trimEnd()
        metadataFile.writeText(metadata)
        println("SyncRepository: Saved photo metadata with serverId: $serverId")
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
                
                // Check if this entry is marked for deletion locally
                val pendingEntries = getPendingEntries()
                val expectedLocalId = "${localMetricName}_${date}_${serverEntry.value}"
                val markedForDeletion = pendingEntries.any { pendingEntry ->
                    // Check by server ID if available
                    if (pendingEntry.serverId == serverEntry.id && pendingEntry.syncStatus == SyncStatus.DELETED_LOCALLY) {
                        return@any true
                    }
                    
                    // Check by consistent localId format
                    if (pendingEntry.localId == expectedLocalId && pendingEntry.syncStatus == SyncStatus.DELETED_LOCALLY) {
                        return@any true
                    }
                    
                    // Fallback: Check by value, date, and optional weight/height matching
                    if (pendingEntry.syncStatus == SyncStatus.DELETED_LOCALLY) {
                        val pendingDate = try {
                            dateFormat.parse(pendingEntry.date)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                        
                        val valuesMatch = abs(pendingEntry.value - serverEntry.value) < 0.01f
                        val datesMatch = abs(pendingDate - date) < 60000 // Within 1 minute
                        
                        return@any valuesMatch && datesMatch
                    }
                    
                    false
                }
                
                if (markedForDeletion) {
                    // Skip this entry as it's marked for deletion locally
                    return
                }
                
                // Check if we already have this entry locally
                val localHistory = metricsRepository.getMetricHistory(localMetricName, getUnitForMetric(localMetricName))
                val existingEntry = localHistory.find { entry ->
                    abs(entry.date - date) < 60000 && // Within 1 minute
                    abs(entry.value - serverEntry.value) < 0.01f // Same value
                }
                
                // Check if this entry is already in our pending list (to avoid duplicates)
                val localId = "${localMetricName}_${date}_${serverEntry.value}"
                val alreadyInPending = pendingEntries.any { pendingEntry ->
                    pendingEntry.serverId == serverEntry.id ||
                    pendingEntry.localId == localId ||
                    (abs(pendingEntry.value - serverEntry.value) < 0.01f && 
                     abs((try { dateFormat.parse(pendingEntry.date)?.time ?: 0L } catch (e: Exception) { 0L }) - date) < 60000)
                }
                
                if (existingEntry == null && !alreadyInPending) {
                    // Save new entry from server
                    metricsRepository.saveMetricHistory(
                        metricName = localMetricName,
                        value = serverEntry.value,
                        unit = getUnitForMetric(localMetricName),
                        date = date
                    )
                    
                    // Mark as synced in pending list with consistent localId format
                    val syncEntry = SyncMetricEntry(
                        localId = localId,
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
                date = toIso8601Utc(entry.date.toLongOrNull() ?: System.currentTimeMillis()),
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
        
        // Use a more precise localId that includes the value to distinguish between entries
        val localId = "${historyEntry.metricName}_${historyEntry.date}_${historyEntry.value}"
        
        val syncEntry = SyncMetricEntry(
            localId = localId,
            metricTypeId = metricTypeId,
            value = historyEntry.value,
            date = dateFormat.format(java.util.Date(historyEntry.date)),
            syncStatus = SyncStatus.PENDING,
            weight = historyEntry.weight,
            height = historyEntry.height
        )
        
        addToPendingEntries(syncEntry)
    }
    
    suspend fun queueForDeletion(historyEntry: HistoryEntry) {
        val metricTypeId = getMetricTypeId(historyEntry.metricName)
        if (metricTypeId == null) {
            return
        }
        
        // Use the same localId format as queueForSync
        val localId = "${historyEntry.metricName}_${historyEntry.date}_${historyEntry.value}"
        
        // Find the entry in pending list and mark for deletion
        val pendingEntries = getPendingEntries().toMutableList()
        val existingEntry = pendingEntries.find { 
            it.localId == localId 
        }
        
        if (existingEntry != null) {
            val updatedEntry = existingEntry.copy(syncStatus = SyncStatus.DELETED_LOCALLY)
            pendingEntries.removeAll { it.localId == existingEntry.localId }
            pendingEntries.add(updatedEntry)
            savePendingEntries(pendingEntries)
        } else {
            // Entry is not in pending list, create a new deletion entry
            val deletionEntry = SyncMetricEntry(
                localId = localId,
                metricTypeId = metricTypeId,
                value = historyEntry.value,
                date = dateFormat.format(java.util.Date(historyEntry.date)),
                syncStatus = SyncStatus.DELETED_LOCALLY,
                // If we have weight/height info, include it for better matching
                weight = historyEntry.weight,
                height = historyEntry.height
            )
            pendingEntries.add(deletionEntry)
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
    
    // --- IMAGE SYNC METHODS ---
    // Image Type IDs (must match database image_types table):
    // 1: front, 2: back, 3: side, 4: biceps, 5: chest, 6: legs, 7: full body, 8: other
    suspend fun queueImageForSync(filePath: String, imageTypeId: Int, date: String) {
        val localId = "$filePath-$imageTypeId-$date"
        println("SyncRepository: Queueing image for sync - localId: $localId, filePath: $filePath, imageTypeId: $imageTypeId")
        
        // Verify file exists
        val file = java.io.File(filePath)
        if (!file.exists()) {
            println("SyncRepository: ERROR - Image file does not exist: $filePath")
            return
        }
        println("SyncRepository: File verification passed - size: ${file.length()} bytes")
        
        val syncEntry = SyncImageEntry(
            localId = localId,
            filePath = filePath,
            imageTypeId = imageTypeId,
            date = date,
            syncStatus = SyncStatus.PENDING
        )
        addToPendingImageEntries(syncEntry)
        println("SyncRepository: Image added to pending entries. Current pending count: ${getPendingImageEntries().size}")
    }

    suspend fun queueImageForDeletion(filePath: String, imageTypeId: Int, date: String, serverId: String? = null) {
        val localId = "$filePath-$imageTypeId-$date"
        println("SyncRepository: queueImageForDeletion called - localId: $localId, serverId: $serverId")
        println("SyncRepository: Input parameters - filePath: $filePath, imageTypeId: $imageTypeId, date: $date")
        
        // Find the entry in pending list and mark for deletion
        val pendingImages = getPendingImageEntries().toMutableList()
        println("SyncRepository: Current pending images count: ${pendingImages.size}")
        
        val existingEntry = pendingImages.find { 
            val localIdMatch = it.localId == localId
            val serverIdMatch = serverId != null && it.serverId == serverId
            println("SyncRepository: Checking entry - localId: ${it.localId}, serverId: ${it.serverId}, status: ${it.syncStatus}")
            println("SyncRepository: Match results - localIdMatch: $localIdMatch, serverIdMatch: $serverIdMatch")
            localIdMatch || serverIdMatch
        }
        
        if (existingEntry != null) {
            println("SyncRepository: Found existing entry to update - current status: ${existingEntry.syncStatus}")
            val updatedEntry = existingEntry.copy(syncStatus = SyncStatus.DELETED_LOCALLY)
            pendingImages.removeAll { it.localId == existingEntry.localId }
            pendingImages.add(updatedEntry)
            savePendingImageEntries(pendingImages)
            println("SyncRepository: Updated existing image entry to DELETED_LOCALLY - new status: ${updatedEntry.syncStatus}")
        } else {
            println("SyncRepository: No existing entry found, creating new deletion entry")
            // Entry is not in pending list, create a new deletion entry
            val deletionEntry = SyncImageEntry(
                localId = localId,
                serverId = serverId,
                filePath = filePath,
                imageTypeId = imageTypeId,
                date = date,
                syncStatus = SyncStatus.DELETED_LOCALLY
            )
            pendingImages.add(deletionEntry)
            savePendingImageEntries(pendingImages)
            println("SyncRepository: Created new image deletion entry - serverId: ${deletionEntry.serverId}, status: ${deletionEntry.syncStatus}")
        }
        
        val finalPendingImages = getPendingImageEntries()
        println("SyncRepository: Final pending images count: ${finalPendingImages.size}")
        finalPendingImages.forEach { entry ->
            println("SyncRepository: Pending entry - localId: ${entry.localId}, serverId: ${entry.serverId}, status: ${entry.syncStatus}")
        }
    }

    private suspend fun getPendingImageEntries(): List<SyncImageEntry> {
        val preferences = context.syncDataStore.data.first()
        val entriesJson = preferences[PENDING_IMAGE_ENTRIES_KEY] ?: return emptyList()
        return try {
            json.decodeFromString<List<SyncImageEntry>>(entriesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun addToPendingImageEntries(entry: SyncImageEntry) {
        val currentEntries = getPendingImageEntries().toMutableList()
        currentEntries.removeAll { it.localId == entry.localId }
        currentEntries.add(entry)
        savePendingImageEntries(currentEntries)
    }

    private suspend fun savePendingImageEntries(entries: List<SyncImageEntry>) {
        context.syncDataStore.edit { preferences ->
            preferences[PENDING_IMAGE_ENTRIES_KEY] = json.encodeToString(entries)
        }
    }

    private suspend fun updateImageEntryAfterSync(entry: SyncImageEntry, status: SyncStatus) {
        val currentEntries = getPendingImageEntries().toMutableList()
        val index = currentEntries.indexOfFirst { it.localId == entry.localId }
        if (index != -1) {
            currentEntries[index] = entry.copy(
                syncStatus = status,
                lastSyncAttempt = System.currentTimeMillis()
            )
            savePendingImageEntries(currentEntries)
        }
    }

    private suspend fun updateImageEntryAfterUpload(entry: SyncImageEntry, serverId: String?, status: SyncStatus) {
        val currentEntries = getPendingImageEntries().toMutableList()
        val index = currentEntries.indexOfFirst { it.localId == entry.localId }
        if (index != -1) {
            currentEntries[index] = entry.copy(
                serverId = serverId,
                syncStatus = status,
                lastSyncAttempt = System.currentTimeMillis()
            )
            savePendingImageEntries(currentEntries)
        }
    }

    private suspend fun removeImageEntryFromPending(entry: SyncImageEntry) {
        val currentEntries = getPendingImageEntries().toMutableList()
        currentEntries.removeAll { it.localId == entry.localId }
        savePendingImageEntries(currentEntries)
    }

    private suspend fun uploadImageEntry(accessToken: String, entry: SyncImageEntry): Result<String?> {
        return try {
            println("SyncRepository: uploadImageEntry called with filePath: ${entry.filePath}, imageTypeId: ${entry.imageTypeId}")
            val file = java.io.File(entry.filePath)
            if (!file.exists()) {
                println("SyncRepository: Image file not found: ${entry.filePath}")
                return Result.failure(Exception("Image file not found: ${entry.filePath}"))
            }
            println("SyncRepository: File exists, size: ${file.length()} bytes")
            
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val imageTypeIdBody = entry.imageTypeId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            println("SyncRepository: Making API call to upload image to /api/images...")
            println("SyncRepository: Using access token: ${accessToken.take(20)}...")
            println("SyncRepository: File details - name: ${file.name}, path: ${file.absolutePath}")
            
            val response = imagesApiService.uploadImage(body, imageTypeIdBody)
            println("SyncRepository: API response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
            
            if (response.isSuccessful) {
                println("SyncRepository: Image upload successful")
                
                // Try to parse the response to get the server ID
                val responseBody = response.body()?.string()
                val serverId = try {
                    if (responseBody != null) {
                        val jsonResponse = json.decodeFromString<Map<String, Any>>(responseBody)
                        // Convert number to string for consistency
                        (jsonResponse["id"] as? Number)?.toString()
                    } else null
                } catch (e: Exception) {
                    println("SyncRepository: Could not parse server ID from response: ${e.message}")
                    null
                }
                
                println("SyncRepository: Parsed server ID: $serverId")
                Result.success(serverId)
            } else {
                val errorBody = response.errorBody()?.string()
                println("SyncRepository: Image upload failed - error: $errorBody")
                
                // Check for specific database constraint errors
                if (response.code() == 500 && errorBody?.contains("foreign key constraint") == true) {
                    if (errorBody.contains("images_image_type_id_fkey")) {
                        return Result.failure(Exception("Invalid image type ID: ${entry.imageTypeId}. The image type does not exist in the database."))
                    }
                }
                
                // Check for authentication errors
                if (response.code() == 401) {
                    if (errorBody?.contains("user not found") == true || 
                        errorBody?.contains("Unauthorized") == true ||
                        errorBody?.contains("User not logged in") == true) {
                        return Result.failure(Exception("Authentication failed - please log in again"))
                    }
                }
                
                Result.failure(Exception(errorBody ?: "Image upload failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            println("SyncRepository: Exception during image upload: ${e.message}")
            e.printStackTrace()
            
            // Check if this is likely an authentication-related exception
            if (e.message?.contains("closed") == true || e.message?.contains("401") == true) {
                return Result.failure(Exception("Upload failed - authentication issue, please log in again"))
            }
            
            Result.failure(e)
        }
    }

    private suspend fun deleteImageOnServer(accessToken: String, serverId: String): Result<Unit> {
        return try {
            println("SyncRepository: deleteImageOnServer called with serverId: $serverId")
            
            // Convert string ID to integer for API call
            val imageIdInt = try {
                serverId.toInt()
            } catch (e: NumberFormatException) {
                println("SyncRepository: Invalid server ID format: $serverId")
                return Result.failure(Exception("Invalid server ID format: $serverId"))
            }
            
            println("SyncRepository: Making DELETE request to /api/images/$imageIdInt")
            val response = imagesApi.deleteImage(imageIdInt)
            println("SyncRepository: Delete API response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                println("SyncRepository: Image deletion successful - Response: $responseBody")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                println("SyncRepository: Image deletion failed - HTTP ${response.code()}")
                println("SyncRepository: Error response body: $errorBody")
                println("SyncRepository: Response headers: ${response.headers()}")
                
                // Check for authentication errors
                if (response.code() == 401) {
                    if (errorBody?.contains("user not found") == true || 
                        errorBody?.contains("Unauthorized") == true ||
                        errorBody?.contains("User not logged in") == true) {
                        println("SyncRepository: Authentication failed during image deletion")
                        return Result.failure(Exception("Authentication failed - please log in again"))
                    }
                }
                
                // If image not found (404), consider it as successful deletion (already deleted)
                if (response.code() == 404) {
                    println("SyncRepository: Image not found on server (404), considering deletion successful")
                    return Result.success(Unit)
                }
                
                Result.failure(Exception(errorBody ?: "Image deletion failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            println("SyncRepository: Exception during image deletion: ${e.message}")
            e.printStackTrace()
            
            // Check if this is likely an authentication-related exception
            if (e.message?.contains("closed") == true || e.message?.contains("401") == true) {
                return Result.failure(Exception("Delete failed - authentication issue, please log in again"))
            }
            
            Result.failure(e)
        }
    }
}
