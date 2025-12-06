package com.example.weedx.data.api

import com.example.weedx.data.models.response.AlertsResponse
import com.example.weedx.data.models.response.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AlertsApiService {
    
    @GET("alerts/all")
    suspend fun getAllAlerts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<AlertsResponse>>
}
