package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

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
    @SerializedName("avatar", alternate = ["avatarUrl"])
    val avatarUrl: String?,
    @SerializedName("joined", alternate = ["createdAt", "created_at"])
    val createdAt: String
)

data class FarmInfo(
    val id: Int,
    val name: String,
    val location: String?,
    @SerializedName("size", alternate = ["area"])
    val area: Double?,
    @SerializedName("crop_types", alternate = ["cropTypes"])
    val cropTypes: String?
)

data class UserSettings(
    @SerializedName("notifications_enabled", alternate = ["notifications"])
    val notifications: Boolean,
    @SerializedName("theme")
    val darkMode: Boolean,
    val language: String,
    @SerializedName("email_alerts")
    val units: String
)
