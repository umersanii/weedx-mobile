package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.ReportsResponse
import com.example.weedx.data.models.response.ReportWidgets
import com.example.weedx.data.models.response.TrendData
import com.example.weedx.data.models.response.DistributionData
import com.example.weedx.data.models.response.ExportResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportsApiService {
    @GET("reports")
    suspend fun getReports(): Response<ApiResponse<ReportsResponse>>
    
    @GET("reports/widgets")
    suspend fun getWidgets(): Response<ApiResponse<ReportWidgets>>
    
    @GET("reports/weed-trend")
    suspend fun getWeedTrend(
        @Query("days") days: Int? = null
    ): Response<ApiResponse<List<TrendData>>>
    
    @GET("reports/weed-distribution")
    suspend fun getWeedDistribution(): Response<ApiResponse<List<DistributionData>>>
    
    @GET("reports/export")
    suspend fun exportReport(
        @Query("format") format: String = "pdf"
    ): Response<ApiResponse<ExportResponse>>
}
