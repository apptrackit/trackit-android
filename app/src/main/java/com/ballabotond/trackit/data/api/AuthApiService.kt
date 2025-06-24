package com.ballabotond.trackit.data.api

import com.ballabotond.trackit.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("user/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
    
    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authToken: String,
        @Body request: LogoutRequest
    ): Response<LogoutResponse>
    
    @GET("auth/check")
    suspend fun checkSession(@Header("Authorization") authToken: String): Response<Map<String, Any>>
}
