package com.ballabotond.trackit.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ballabotond.trackit.data.api.AuthApiService
import com.ballabotond.trackit.data.model.*
import com.ballabotond.trackit.data.network.NetworkModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthRepository(private val context: Context) {
    private val authApi: AuthApiService = NetworkModule.authApiService
    
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }
    
    // Generate or get device ID
    suspend fun getDeviceId(): String {
        val preferences = context.dataStore.data.first()
        val deviceId = preferences[DEVICE_ID_KEY]
        return if (deviceId != null) {
            deviceId
        } else {
            val newDeviceId = UUID.randomUUID().toString()
            saveDeviceId(newDeviceId)
            newDeviceId
        }
    }
    
    private suspend fun saveDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_ID_KEY] = deviceId
        }
    }
    
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.success && loginResponse.accessToken != null) {
                    // Save tokens and user info
                    saveAuthData(loginResponse)
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception(loginResponse.message ?: "Login failed"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, password: String, email: String): Result<RegisterResponse> {
        return try {
            val response = authApi.register(RegisterRequest(username, password, email))
            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!
                Result.success(registerResponse)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun saveAuthData(loginResponse: LoginResponse) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = loginResponse.accessToken ?: ""
            preferences[REFRESH_TOKEN_KEY] = loginResponse.refreshToken ?: ""
            preferences[IS_LOGGED_IN_KEY] = true
            
            loginResponse.user?.let { user ->
                preferences[USER_ID_KEY] = user.id
                preferences[USERNAME_KEY] = user.username
                preferences[EMAIL_KEY] = user.email
            }
        }
    }
    
    suspend fun getAccessToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[ACCESS_TOKEN_KEY]
    }
    
    suspend fun getRefreshToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[REFRESH_TOKEN_KEY]
    }
    
    suspend fun isLoggedIn(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[IS_LOGGED_IN_KEY] ?: false
    }
    
    fun isLoggedInFlow(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }
    }
    
    suspend fun getCurrentUser(): User? {
        val preferences = context.dataStore.data.first()
        val userId = preferences[USER_ID_KEY]
        val username = preferences[USERNAME_KEY]
        val email = preferences[EMAIL_KEY]
        
        return if (userId != null && username != null && email != null) {
            User(userId, username, email)
        } else {
            null
        }
    }
    
    suspend fun refreshToken(): Result<String> {
        return try {
            val refreshToken = getRefreshToken()
            val deviceId = getDeviceId()
            
            if (refreshToken == null) {
                return Result.failure(Exception("No refresh token available"))
            }
            
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken, deviceId))
            if (response.isSuccessful && response.body() != null) {
                val refreshResponse = response.body()!!
                if (refreshResponse.success && refreshResponse.accessToken != null) {
                    // Save new access token
                    context.dataStore.edit { preferences ->
                        preferences[ACCESS_TOKEN_KEY] = refreshResponse.accessToken
                    }
                    Result.success(refreshResponse.accessToken)
                } else {
                    Result.failure(Exception(refreshResponse.message ?: "Token refresh failed"))
                }
            } else {
                Result.failure(Exception("Token refresh failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            val accessToken = getAccessToken()
            val deviceId = getDeviceId()
            
            if (accessToken != null) {
                // Try to logout from server
                authApi.logout("Bearer $accessToken", LogoutRequest(deviceId))
            }
            
            // Clear local data regardless of server response
            clearAuthData()
            Result.success(Unit)
        } catch (e: Exception) {
            // Still clear local data even if server request fails
            clearAuthData()
            Result.success(Unit)
        }
    }
    
    private suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(EMAIL_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }
}
