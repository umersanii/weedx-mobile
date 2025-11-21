package com.example.weedx.data.repositories

import com.example.weedx.data.api.MonitoringApiService
import com.example.weedx.data.models.response.Activity
import com.example.weedx.data.models.response.Location
import com.example.weedx.data.models.response.Metrics
import com.example.weedx.data.models.response.MonitoringResponse
import com.example.weedx.utils.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoringRepository @Inject constructor(
    private val monitoringApiService: MonitoringApiService
) {
    suspend fun getMonitoring(): NetworkResult<MonitoringResponse> {
        return try {
            val response = monitoringApiService.getMonitoring()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch monitoring data")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getMetrics(): NetworkResult<Metrics> {
        return try {
            val response = monitoringApiService.getMetrics()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch metrics")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getActivity(): NetworkResult<List<Activity>> {
        return try {
            val response = monitoringApiService.getActivity()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch activity")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getLocation(): NetworkResult<Location> {
        return try {
            val response = monitoringApiService.getLocation()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch location")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
