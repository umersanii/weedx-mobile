package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.MonitoringResponse
import com.example.weedx.data.models.response.Metrics
import com.example.weedx.data.models.response.Activity
import com.example.weedx.data.models.response.Location
import retrofit2.Response
import retrofit2.http.GET

interface MonitoringApiService {
    @GET("monitoring")
    suspend fun getMonitoring(): Response<ApiResponse<MonitoringResponse>>
    
    @GET("monitoring/metrics")
    suspend fun getMetrics(): Response<ApiResponse<Metrics>>
    
    @GET("monitoring/activity")
    suspend fun getActivity(): Response<ApiResponse<List<Activity>>>
    
    @GET("monitoring/location")
    suspend fun getLocation(): Response<ApiResponse<Location>>
}
