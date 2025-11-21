package com.example.weedx.data.repositories

import com.example.weedx.data.api.DashboardApiService
import com.example.weedx.data.models.response.Alert
import com.example.weedx.data.models.response.DashboardResponse
import com.example.weedx.data.models.response.RobotStatus
import com.example.weedx.data.models.response.TodaySummary
import com.example.weedx.utils.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val dashboardApiService: DashboardApiService
) {
    suspend fun getDashboard(): NetworkResult<DashboardResponse> {
        return try {
            val response = dashboardApiService.getDashboard()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch dashboard")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getRobotStatus(): NetworkResult<RobotStatus> {
        return try {
            val response = dashboardApiService.getRobotStatus()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch robot status")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getTodaySummary(): NetworkResult<TodaySummary> {
        return try {
            val response = dashboardApiService.getTodaySummary()
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
    
    suspend fun getRecentAlerts(): NetworkResult<List<Alert>> {
        return try {
            val response = dashboardApiService.getRecentAlerts()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch alerts")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
