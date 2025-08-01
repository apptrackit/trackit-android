package com.ballabotond.trackit.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MetricTypeMapping(
    val localName: String,
    val serverTypeId: Int
)

@Serializable
data class SyncMetricEntry(
    val localId: String? = null,
    val serverId: String? = null,
    val metricTypeId: Int,
    val value: Float,
    val date: String, // ISO 8601 format
    val isAppleHealth: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncAttempt: Long = 0L,
    val weight: Float? = null,
    val height: Float? = null
)

@Serializable
data class SyncImageEntry(
    val localId: String? = null,
    val serverId: String? = null,
    val filePath: String,
    val imageTypeId: Int,
    val date: String, // ISO 8601 format
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncAttempt: Long = 0L
)

@Serializable
enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED,
    DELETED_LOCALLY,
    DELETED_ON_SERVER
}

data class CreateMetricRequest(
    val metric_type_id: Int,
    val value: Float,
    val date: String,
    val is_apple_health: Boolean = false
)

data class UpdateMetricRequest(
    val value: Float,
    val date: String,
    val is_apple_health: Boolean = false
)

data class MetricResponse(
    val success: Boolean,
    val message: String? = null,
    val entryId: String? = null
)

data class ServerMetricEntry(
    val id: String,
    val metric_type_id: Int,
    val value: Float,
    val date: String,
    val is_apple_health: Boolean
)

data class GetMetricsResponse(
    val success: Boolean,
    val entries: List<ServerMetricEntry>,
    val total: Int
)

data class MetricType(
    val id: Int,
    val name: String,
    val unit: String
)

data class GetMetricTypesResponse(
    val success: Boolean,
    val types: List<MetricType>
)

data class SyncState(
    val lastSyncTimestamp: Long = 0L,
    val isOnline: Boolean = false,
    val isSyncing: Boolean = false,
    val pendingUploads: Int = 0,
    val failedUploads: Int = 0
)
