package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.WeedLogsResponse
import com.example.weedx.data.models.response.WeedSummary
import com.example.weedx.data.models.response.WeedDetection
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeedLogsApiService {
    @GET("weed-logs")
    suspend fun getWeedLogs(): Response<ApiResponse<WeedLogsResponse>>
    
    @GET("weed-logs/summary")
    suspend fun getSummary(): Response<ApiResponse<List<WeedSummary>>>
    
    @GET("weed-logs/detections")
    suspend fun getDetections(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("weedType") weedType: String? = null
    ): Response<ApiResponse<List<WeedDetection>>>
}
