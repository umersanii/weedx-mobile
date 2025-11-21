package com.example.weedx.data.repositories

import com.example.weedx.data.api.ReportsApiService
import com.example.weedx.data.models.response.DistributionData
import com.example.weedx.data.models.response.ExportResponse
import com.example.weedx.data.models.response.ReportWidgets
import com.example.weedx.data.models.response.ReportsResponse
import com.example.weedx.data.models.response.TrendData
import com.example.weedx.utils.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRepository @Inject constructor(
    private val reportsApiService: ReportsApiService
) {
    suspend fun getReports(): NetworkResult<ReportsResponse> {
        return try {
            val response = reportsApiService.getReports()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch reports")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getWidgets(): NetworkResult<ReportWidgets> {
        return try {
            val response = reportsApiService.getWidgets()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch widgets")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getWeedTrend(days: Int? = null): NetworkResult<List<TrendData>> {
        return try {
            val response = reportsApiService.getWeedTrend(days)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch trend")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getWeedDistribution(): NetworkResult<List<DistributionData>> {
        return try {
            val response = reportsApiService.getWeedDistribution()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch distribution")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun exportReport(format: String = "pdf"): NetworkResult<ExportResponse> {
        return try {
            val response = reportsApiService.exportReport(format)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to export report")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
