package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.ProfileResponse
import com.example.weedx.data.models.response.UserProfile
import com.example.weedx.data.models.response.FarmInfo
import com.example.weedx.data.models.response.UserSettings
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {
    @GET("profile")
    suspend fun getProfile(): Response<ApiResponse<ProfileResponse>>
    
    @PUT("profile")
    suspend fun updateProfile(
        @Body profile: Map<String, Any>
    ): Response<ApiResponse<UserProfile>>
    
    @Multipart
    @PATCH("profile/avatar")
    suspend fun updateAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<ApiResponse<String>>
    
    @GET("profile/farm")
    suspend fun getFarmInfo(): Response<ApiResponse<FarmInfo>>
    
    @PUT("profile/farm")
    suspend fun updateFarmInfo(
        @Body farmInfo: Map<String, Any>
    ): Response<ApiResponse<FarmInfo>>
    
    @GET("profile/settings")
    suspend fun getSettings(): Response<ApiResponse<UserSettings>>
    
    @PUT("profile/settings")
    suspend fun updateSettings(
        @Body settings: Map<String, Any>
    ): Response<ApiResponse<UserSettings>>
}
