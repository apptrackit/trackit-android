package com.ballabotond.trackit.data.network

import android.content.Context
import com.ballabotond.trackit.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context, private val authRepository: AuthRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { authRepository.getAccessToken() }
        println("AuthInterceptor: Token retrieved: ${if (token.isNullOrEmpty()) "EMPTY/NULL" else "Present (${token.take(20)}...)"}")
        
        if (token.isNullOrEmpty()) {
            println("AuthInterceptor: No token found, proceeding without auth header")
            return chain.proceed(original)
        }
        
        val request = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        println("AuthInterceptor: Added Authorization header for ${original.url}")
        
        val response = chain.proceed(request)
        
        // If we get a 401, try to refresh the token and retry
        if (response.code == 401) {
            println("AuthInterceptor: Got 401, attempting token refresh")
            response.close()
            
            val refreshResult = runBlocking { authRepository.refreshToken() }
            if (refreshResult.isSuccess) {
                val newToken = refreshResult.getOrNull()
                if (!newToken.isNullOrEmpty()) {
                    println("AuthInterceptor: Token refreshed successfully, retrying request")
                    val newRequest = original.newBuilder()
                        .addHeader("Authorization", "Bearer $newToken")
                        .build()
                    return chain.proceed(newRequest)
                }
            }
            println("AuthInterceptor: Token refresh failed")
        }
        
        return response
    }
}
