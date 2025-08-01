package com.ballabotond.trackit.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImagesApiService {
    @Multipart
    @POST("api/images")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("imageTypeId") imageTypeId: RequestBody
    ): Response<ResponseBody>
}
