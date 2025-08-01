package com.ballabotond.trackit.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ballabotond.trackit.data.model.Photo
import com.ballabotond.trackit.data.model.PhotoCategory
import com.ballabotond.trackit.data.model.PhotoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.media.ExifInterface
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * ViewModel responsible for managing photos and their metadata.
 * Handles loading, saving, updating, and deleting photos with their associated categories.
 */
class PhotoViewModel : ViewModel() {
    /**
     * Save photo and immediately queue for upload to backend.
     * @param context The application context
     * @param uri The image URI
     * @param category The photo category
     * @param syncRepository The SyncRepository instance
     */
    fun savePhotoAndUpload(
        context: Context,
        uri: Uri,
        category: PhotoCategory,
        syncRepository: com.ballabotond.trackit.data.repository.SyncRepository
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Save photo first and get the file path directly
                val photosDir = ensureDirectoryExists(context, PHOTOS_DIR)
                val metadataDir = ensureDirectoryExists(context, METADATA_DIR)

                // Try to get EXIF date
                val exifDate = getExifDate(context, uri)
                val timestamp = exifDate ?: System.currentTimeMillis()
                val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                val fileName = "IMG_${dateFormat.format(Date(timestamp))}.jpg"
                val photoFile = File(photosDir, fileName)

                savePhotoFile(context, uri, photoFile)
                photoFile.setLastModified(timestamp)
                savePhotoMetadata(metadataDir, fileName, category, exifTimestamp = timestamp)

                // Now we have the actual file path
                val filePath = photoFile.absolutePath
                val imageTypeId = when (category) {
                    PhotoCategory.FRONT -> 1
                    PhotoCategory.BACK -> 2
                    PhotoCategory.SIDE -> 3
                    PhotoCategory.BICEPS -> 4
                    PhotoCategory.CHEST -> 5
                    PhotoCategory.LEGS -> 6
                    PhotoCategory.FULL_BODY -> 7
                    else -> 8 // OTHER - matches database
                }
                val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
                
                println("PhotoViewModel: About to queue image for sync")
                println("PhotoViewModel: - Category: $category -> ImageTypeId: $imageTypeId")
                println("PhotoViewModel: - FilePath: $filePath")
                println("PhotoViewModel: - File exists: ${photoFile.exists()}")
                println("PhotoViewModel: - File size: ${if (photoFile.exists()) photoFile.length() else "N/A"} bytes")
                println("PhotoViewModel: - Date: $date")
                
                // Queue for sync
                syncRepository.queueImageForSync(filePath, imageTypeId, date)
                println("PhotoViewModel: Image queued for sync, now calling syncAllData()")
                
                val result = syncRepository.syncAllData()
                result.fold(
                    onSuccess = { println("PhotoViewModel: Sync completed successfully") },
                    onFailure = { error -> 
                        println("PhotoViewModel: Sync failed with error: ${error.message}")
                        // Check if this is an authentication error
                        if (error.message?.contains("User not logged in") == true || 
                            error.message?.contains("No access token") == true) {
                            println("PhotoViewModel: Authentication error detected - user needs to log in again")
                        }
                    }
                )
                
                // Update the photos list after sync attempt
                loadPhotos(context)
            }
        }
    }
    // State properties
    var photos by mutableStateOf<List<Photo>>(emptyList())
        private set
    
    var filteredPhotos by mutableStateOf<List<Photo>>(emptyList())
        private set
        
    var selectedCategory by mutableStateOf<PhotoCategory?>(null)
        
    private var isLoading by mutableStateOf(false)

    // Directory names
    private companion object {
        const val PHOTOS_DIR = "photos"
        const val METADATA_DIR = "photo_metadata"
        const val DATE_FORMAT = "yyyyMMdd_HHmmss"
    }

    /**
     * Loads all photos from the device storage and updates the state.
     * @param context The application context
     */
    fun loadPhotos(context: Context) {
        if (isLoading) return
        
        viewModelScope.launch {
            isLoading = true
            delay(300) // Prevent immediate loading when quickly swiping
            
            withContext(Dispatchers.IO) {
                val photosDir = ensureDirectoryExists(context, PHOTOS_DIR)
                val metadataDir = ensureDirectoryExists(context, METADATA_DIR)
                
                val photoList = photosDir.listFiles()?.mapNotNull { file ->
                    createPhotoFromFile(file, metadataDir)
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                
                photos = photoList
                applyFilter()
            }
            isLoading = false
        }
    }

    /**
     * Saves a new photo with its category to the device storage.
     * @param context The application context
     * @param uri The URI of the photo to save
     * @param category The category of the photo (defaults to OTHER)
     */
    fun savePhoto(context: Context, uri: Uri, category: PhotoCategory = PhotoCategory.OTHER) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val photosDir = ensureDirectoryExists(context, PHOTOS_DIR)
                val metadataDir = ensureDirectoryExists(context, METADATA_DIR)

                // Try to get EXIF date
                val exifDate = getExifDate(context, uri)
                val timestamp = exifDate ?: System.currentTimeMillis()
                val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                val fileName = "IMG_${dateFormat.format(Date(timestamp))}.jpg"
                val photoFile = File(photosDir, fileName)

                savePhotoFile(context, uri, photoFile)
                // Set file's last modified to EXIF date if available
                photoFile.setLastModified(timestamp)
                savePhotoMetadata(metadataDir, fileName, category, exifTimestamp = timestamp)

                // Show toast with EXIF date if available
                if (exifDate != null) {
                    Handler(Looper.getMainLooper()).post {
                        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(exifDate))
                        Toast.makeText(context, "Photo date: $dateStr", Toast.LENGTH_LONG).show()
                    }
                }

                loadPhotos(context)
            }
        }
    }
    
    /**
     * Updates the category of an existing photo.
     * @param context The application context
     * @param photo The photo to update
     * @param newCategory The new category for the photo
     */
    fun updatePhotoCategory(context: Context, photo: Photo, newCategory: PhotoCategory) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(photo.filePath)
                if (file.exists()) {
                    val metadataDir = ensureDirectoryExists(context, METADATA_DIR)
                    val metadataFile = File(metadataDir, "${file.name}.json")
                    
                    savePhotoMetadata(metadataDir, file.name, newCategory)
                    loadPhotos(context)
                }
            }
        }
    }

    /**
     * Deletes a photo and its metadata from the device storage.
     * @param context The application context
     * @param photo The photo to delete
     */
    fun deletePhoto(context: Context, photo: Photo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(photo.filePath)
                if (file.exists()) {
                    file.delete()
                    
                    val metadataDir = ensureDirectoryExists(context, METADATA_DIR)
                    val metadataFile = File(metadataDir, "${file.name}.json")
                    metadataFile.delete()
                    
                    loadPhotos(context)
                }
            }
        }
    }
    
    /**
     * Gets all photos of a specific category.
     * @param category The category to filter by
     * @return List of photos in the specified category
     */
    fun getPhotosOfCategory(category: PhotoCategory): List<Photo> {
        return photos.filter { it.category == category }
    }

    // Private helper methods
    private fun ensureDirectoryExists(context: Context, dirName: String): File {
        val dir = File(context.filesDir, dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun createPhotoFromFile(file: File, metadataDir: File): Photo? {
        val uri = Uri.fromFile(file)
        val filePath = file.absolutePath
        val timestamp = Date(file.lastModified())
        
        val metadataFile = File(metadataDir, "${file.name}.json")
        val metadata = if (metadataFile.exists()) {
            try {
                val json = JSONObject(metadataFile.readText())
                val category = PhotoCategory.fromString(json.optString("category", PhotoCategory.OTHER.name))
                
                // Parse additional metadata
                val photoMetadata = PhotoMetadata(
                    weight = json.optDouble("weight", 0.0).takeIf { it > 0 }?.toFloat(),
                    bodyFatPercentage = json.optDouble("bodyFatPercentage", 0.0).takeIf { it > 0 }?.toFloat(),
                    notes = json.optString("notes", "")
                )
                
                // Parse measurements if they exist
                val measurementsObj = json.optJSONObject("measurements")
                val measurements = mutableMapOf<String, Float>()
                if (measurementsObj != null) {
                    measurementsObj.keys().forEach { key ->
                        measurements[key] = measurementsObj.optDouble(key, 0.0).toFloat()
                    }
                }
                
                Photo(uri, category, timestamp, filePath, photoMetadata.copy(measurements = measurements))
            } catch (e: Exception) {
                Photo(uri, PhotoCategory.OTHER, timestamp, filePath)
            }
        } else {
            Photo(uri, PhotoCategory.OTHER, timestamp, filePath)
        }
        
        return metadata
    }

    private fun savePhotoFile(context: Context, uri: Uri, photoFile: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            // First, decode the image to get its dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            
            // Calculate compression ratio to keep image under 2MB
            val maxFileSize = 2 * 1024 * 1024 // 2MB
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            
            // Calculate scale factor
            var scaleFactor = 1
            if (originalHeight > 1920 || originalWidth > 1920) {
                val heightRatio = originalHeight / 1920
                val widthRatio = originalWidth / 1920
                scaleFactor = if (heightRatio > widthRatio) heightRatio else widthRatio
            }
            
            // Decode the actual bitmap with scaling
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val decodingOptions = BitmapFactory.Options().apply {
                    inSampleSize = scaleFactor
                }
                val bitmap = BitmapFactory.decodeStream(inputStream, null, decodingOptions)
                
                if (bitmap != null) {
                    // Start with high quality and reduce if needed
                    var quality = 90
                    var tempFile = File.createTempFile("temp_photo", ".jpg", photoFile.parentFile)
                    
                    do {
                        FileOutputStream(tempFile).use { output ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                        }
                        val fileSizeBytes = tempFile.length()
                        if (fileSizeBytes <= maxFileSize || quality <= 50) {
                            // Copy temp file to final destination
                            tempFile.copyTo(photoFile, overwrite = true)
                            println("PhotoViewModel: Compressed image to ${fileSizeBytes / 1024}KB with quality $quality")
                            break
                        }
                        quality -= 10
                    } while (quality > 50)
                    
                    tempFile.delete()
                    bitmap.recycle()
                } else {
                    // Fallback to original method if bitmap decoding fails
                    context.contentResolver.openInputStream(uri)?.use { fallbackInput ->
                        FileOutputStream(photoFile).use { output ->
                            fallbackInput.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    private fun savePhotoMetadata(
        metadataDir: File,
        fileName: String,
        category: PhotoCategory,
        metadata: PhotoMetadata = PhotoMetadata(),
        exifTimestamp: Long? = null
    ) {
        val metadataFile = File(metadataDir, "$fileName.json")
        val json = JSONObject().apply {
            put("category", category.name)
            put("timestamp", exifTimestamp ?: Date().time)
            // Save additional metadata
            metadata.weight?.let { put("weight", it.toDouble()) }
            metadata.bodyFatPercentage?.let { put("bodyFatPercentage", it.toDouble()) }
            if (metadata.notes.isNotEmpty()) put("notes", metadata.notes)
            
            // Save measurements
            if (metadata.measurements.isNotEmpty()) {
                val measurementsObj = JSONObject()
                metadata.measurements.forEach { (key, value) ->
                    measurementsObj.put(key, value.toDouble())
                }
                put("measurements", measurementsObj)
            }
        }
        metadataFile.writeText(json.toString())
    }

    /**
     * Updates the photo metadata
     * @param context The application context
     * @param photo The photo to update
     * @param metadata The new metadata to save
     */
    fun updatePhotoMetadata(context: Context, photo: Photo, metadata: PhotoMetadata) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(photo.filePath)
                if (file.exists()) {
                    val metadataDir = ensureDirectoryExists(context, METADATA_DIR)
                    savePhotoMetadata(metadataDir, file.name, photo.category, metadata)
                    loadPhotos(context)
                }
            }
        }
    }

    /**
     * Updates the date (timestamp) of a photo and its metadata.
     */
    fun updatePhotoDate(context: Context, photo: Photo, newDateMillis: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(photo.filePath)
                if (file.exists()) {
                    file.setLastModified(newDateMillis)
                    val metadataDir = ensureDirectoryExists(context, METADATA_DIR)
                    val metadataFile = File(metadataDir, "${file.name}.json")
                    if (metadataFile.exists()) {
                        try {
                            val json = JSONObject(metadataFile.readText())
                            json.put("timestamp", newDateMillis)
                            metadataFile.writeText(json.toString())
                        } catch (_: Exception) {}
                    }
                    loadPhotos(context)
                }
            }
        }
    }

    // Helper to extract EXIF date from image
    private fun getExifDate(context: Context, uri: Uri): Long? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val dateStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                if (dateStr != null) {
                    // EXIF date format: "yyyy:MM:dd HH:mm:ss"
                    val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                    sdf.parse(dateStr)?.time
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun applyFilter() {
        filteredPhotos = if (selectedCategory == null) {
            photos
        } else {
            photos.filter { it.category == selectedCategory }
        }
    }
}
