package com.example.weedx.data.models.response

data class ProfileResponse(
    val user: UserProfile,
    val farm: FarmInfo?,
    val settings: UserSettings?
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatarUrl: String?,
    val createdAt: String
)

data class FarmInfo(
    val id: Int,
    val name: String,
    val location: String?,
    val area: Double?,
    val cropTypes: String?
)

data class UserSettings(
    val notifications: Boolean,
    val darkMode: Boolean,
    val language: String,
    val units: String
)
