package com.ballabotond.trackit.data.network

import com.ballabotond.trackit.data.api.AuthApiService
import com.ballabotond.trackit.data.api.MetricsApiService
import com.ballabotond.trackit.data.api.ImagesApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://devtrackit.ballabotond.com"

    fun getRetrofitWithAuth(context: android.content.Context, authRepository: com.ballabotond.trackit.data.repository.AuthRepository): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = AuthInterceptor(context, authRepository)
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS) // Increased for large file uploads
            .writeTimeout(120, TimeUnit.SECONDS) // Increased for large file uploads
            .callTimeout(300, TimeUnit.SECONDS) // 5 minutes total timeout for large uploads
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Special configuration for large file uploads
    fun getRetrofitForImageUploads(context: android.content.Context, authRepository: com.ballabotond.trackit.data.repository.AuthRepository): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Reduce logging for large uploads
        }
        val authInterceptor = AuthInterceptor(context, authRepository)
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS) // Extended for large files
            .readTimeout(300, TimeUnit.SECONDS) // 5 minutes for reading large uploads
            .writeTimeout(300, TimeUnit.SECONDS) // 5 minutes for writing large uploads
            .callTimeout(600, TimeUnit.SECONDS) // 10 minutes total timeout
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val retrofit: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS) // Increased for large file uploads
            .writeTimeout(120, TimeUnit.SECONDS) // Increased for large file uploads
            .callTimeout(300, TimeUnit.SECONDS) // 5 minutes total timeout for large uploads
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    val metricsApiService: MetricsApiService by lazy {
        retrofit.create(MetricsApiService::class.java)
    }
    val imagesApiService: ImagesApiService by lazy {
        retrofit.create(ImagesApiService::class.java)
    }

    // Create ImagesApiService with extended timeouts for large uploads
    fun getImagesApiServiceForUploads(context: android.content.Context, authRepository: com.ballabotond.trackit.data.repository.AuthRepository): ImagesApiService {
        return getRetrofitForImageUploads(context, authRepository).create(ImagesApiService::class.java)
    }
}
