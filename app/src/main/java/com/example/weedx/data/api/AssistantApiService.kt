package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.AssistantQueryResponse
import com.example.weedx.data.models.response.AssistantHistory
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AssistantApiService {
    @POST("assistant/query")
    suspend fun sendQuery(
        @Body request: Map<String, String>
    ): Response<ApiResponse<AssistantQueryResponse>>
    
    @GET("assistant/history")
    suspend fun getHistory(): Response<ApiResponse<List<AssistantHistory>>>
}
