package com.example.weedx.data.repositories

import com.example.weedx.data.api.AlertsApiService
import com.example.weedx.data.models.response.AlertsResponse
import com.example.weedx.utils.NetworkResult
import javax.inject.Inject

class AlertsRepository @Inject constructor(
    private val alertsApiService: AlertsApiService
) {
    
    suspend fun getAllAlerts(page: Int = 1, limit: Int = 50): NetworkResult<AlertsResponse> {
        return try {
            val response = alertsApiService.getAllAlerts(page, limit)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.message ?: "Failed to fetch alerts")
                }
            } else {
                NetworkResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }
}
