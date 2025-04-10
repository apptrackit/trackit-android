package com.example.lifetracker.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoViewModel : ViewModel() {
    var photos by mutableStateOf<List<Uri>>(emptyList())
        private set
    private var isLoading by mutableStateOf(false)

    fun loadPhotos(context: Context) {
        if (isLoading) return
        
        viewModelScope.launch {
            isLoading = true
            // Add a small delay to prevent immediate loading when quickly swiping
            delay(300)
            
            withContext(Dispatchers.IO) {
                val photosDir = File(context.filesDir, "photos")
                if (!photosDir.exists()) {
                    photosDir.mkdirs()
                }
                val photoFiles: Array<File> = photosDir.listFiles() ?: emptyArray()
                photos = photoFiles.sortedByDescending { it.lastModified() }.map { Uri.fromFile(it) }
            }
            isLoading = false
        }
    }

    fun savePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val photosDir = File(context.filesDir, "photos")
                if (!photosDir.exists()) {
                    photosDir.mkdirs()
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val photoFile = File(photosDir, "IMG_$timestamp.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(photoFile).use { output ->
                        input.copyTo(output)
                    }
                }

                loadPhotos(context)
            }
        }
    }

    fun deletePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(uri.path ?: return@withContext)
                if (file.exists()) {
                    file.delete()
                    loadPhotos(context)
                }
            }
        }
    }
} 