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
    val client_uuid: String? = null,
    val value: Float,
    val date: String, // ISO 8601 format
    val source: String? = null,
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
    val client_uuid: String,
    val value: Float,
    val entry_date: String,
    val source: String? = null
)

data class UpdateMetricRequest(
    val value: Float? = null,
    val entry_date: String? = null,
    val source: String? = null
)

data class MetricResponse(
    val success: Boolean,
    val message: String? = null,
    val entryId: String? = null,
    val entry: ServerMetricEntry? = null
)

data class ServerMetricEntry(
    val id: String,
    val metric_type_id: Int,
    val client_uuid: String,
    val value: Float,
    val entry_date: String,
    val source: String?,
    val version: Int,
    val created_at: String,
    val updated_at: String
)

data class GetMetricsResponse(
    val success: Boolean,
    val entries: List<ServerMetricEntry>,
    val total: Int
)

data class MetricType(
    val id: Int,
    val name: String,
    val unit: String,
    val description: String? = null
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

data class ServerImageEntry(
    val id: String,
    val image_type_id: Int,
    val filename: String,
    val date: String,
    val file_size: Int,
    val mime_type: String
)

data class GetImagesResponse(
    val success: Boolean,
    val images: List<ServerImageEntry>,
    val total: Int
)
