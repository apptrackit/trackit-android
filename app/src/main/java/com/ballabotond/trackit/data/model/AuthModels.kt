package com.ballabotond.trackit.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
)

data class LoginResponse(
    val success: Boolean,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val message: String? = null,
    val user: User? = null
)

data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class RefreshTokenRequest(
    val refreshToken: String,
    val deviceId: String
)

data class RefreshTokenResponse(
    val success: Boolean,
    val accessToken: String? = null,
    val message: String? = null
)

data class LogoutRequest(
    val deviceId: String
)

data class LogoutResponse(
    val success: Boolean,
    val message: String? = null
)
