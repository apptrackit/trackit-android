package com.ballabotond.trackit.data.repository

import com.ballabotond.trackit.data.api.ImagesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
class ImagesRepository(
    private val imagesApiService: ImagesApiService
) {
    suspend fun uploadImage(file: File, imageTypeId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val imageTypeIdBody = imageTypeId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val response = imagesApiService.uploadImage(body, imageTypeIdBody)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
