package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.DashboardResponse
import com.example.weedx.data.models.response.RobotStatus
import com.example.weedx.data.models.response.TodaySummary
import com.example.weedx.data.models.response.Alert
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApiService {
    @GET("landing")
    suspend fun getDashboard(): Response<ApiResponse<DashboardResponse>>
    
    @GET("robot/status")
    suspend fun getRobotStatus(): Response<ApiResponse<RobotStatus>>
    
    @GET("summary/today")
    suspend fun getTodaySummary(): Response<ApiResponse<TodaySummary>>
    
    @GET("alerts/recent")
    suspend fun getRecentAlerts(): Response<ApiResponse<List<Alert>>>
}
