package com.example.weedx.data.repositories

import com.example.weedx.data.api.WeedLogsApiService
import com.example.weedx.data.models.response.WeedDetection
import com.example.weedx.data.models.response.WeedLogsResponse
import com.example.weedx.data.models.response.WeedSummary
import com.example.weedx.utils.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeedLogsRepository @Inject constructor(
    private val weedLogsApiService: WeedLogsApiService
) {
    suspend fun getWeedLogs(): NetworkResult<WeedLogsResponse> {
        return try {
            val response = weedLogsApiService.getWeedLogs()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch weed logs")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getSummary(): NetworkResult<List<WeedSummary>> {
        return try {
            val response = weedLogsApiService.getSummary()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch summary")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getDetections(
        page: Int? = null,
        limit: Int? = null,
        weedType: String? = null
    ): NetworkResult<List<WeedDetection>> {
        return try {
            val response = weedLogsApiService.getDetections(page, limit, weedType)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch detections")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
