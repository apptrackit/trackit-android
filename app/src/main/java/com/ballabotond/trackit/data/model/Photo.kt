package com.ballabotond.trackit.data.model

import android.net.Uri
import java.util.*

data class Photo(
    val uri: Uri,
    val category: PhotoCategory = PhotoCategory.OTHER,
    val timestamp: Date = Date(),
    val filePath: String,
    val metadata: PhotoMetadata = PhotoMetadata()
)

data class PhotoMetadata(
    val weight: Float? = null,
    val bodyFatPercentage: Float? = null,
    val notes: String = "",
    val measurements: Map<String, Float> = emptyMap()
) 