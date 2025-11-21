package com.example.weedx.data.repositories

import com.example.weedx.data.api.ProfileApiService
import com.example.weedx.data.models.response.FarmInfo
import com.example.weedx.data.models.response.ProfileResponse
import com.example.weedx.data.models.response.UserProfile
import com.example.weedx.data.models.response.UserSettings
import com.example.weedx.utils.NetworkResult
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileApiService: ProfileApiService
) {
    suspend fun getProfile(): NetworkResult<ProfileResponse> {
        return try {
            val response = profileApiService.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch profile")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun updateProfile(profile: Map<String, Any>): NetworkResult<UserProfile> {
        return try {
            val response = profileApiService.updateProfile(profile)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun updateAvatar(avatar: MultipartBody.Part): NetworkResult<String> {
        return try {
            val response = profileApiService.updateAvatar(avatar)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Success("Avatar updated successfully")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to update avatar")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getFarmInfo(): NetworkResult<FarmInfo> {
        return try {
            val response = profileApiService.getFarmInfo()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch farm info")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun updateFarmInfo(farmInfo: Map<String, Any>): NetworkResult<FarmInfo> {
        return try {
            val response = profileApiService.updateFarmInfo(farmInfo)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to update farm info")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getSettings(): NetworkResult<UserSettings> {
        return try {
            val response = profileApiService.getSettings()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch settings")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun updateSettings(settings: Map<String, Any>): NetworkResult<UserSettings> {
        return try {
            val response = profileApiService.updateSettings(settings)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to update settings")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
