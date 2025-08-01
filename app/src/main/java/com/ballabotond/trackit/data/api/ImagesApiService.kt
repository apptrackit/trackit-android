package com.ballabotond.trackit.data.api

import com.ballabotond.trackit.data.model.GetImagesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ImagesApiService {
    @GET("api/images")
    suspend fun getImages(
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0
    ): Response<GetImagesResponse>
    
    @GET("api/images/{id}/download")
    suspend fun downloadImage(
        @Path("id") imageId: String
    ): Response<ResponseBody>
    
    @Multipart
    @POST("api/images")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("imageTypeId") imageTypeId: RequestBody
    ): Response<ResponseBody>
}
