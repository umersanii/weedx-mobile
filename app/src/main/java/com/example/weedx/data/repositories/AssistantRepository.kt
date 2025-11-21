package com.example.weedx.data.repositories

import com.example.weedx.data.api.AssistantApiService
import com.example.weedx.data.models.response.AssistantHistory
import com.example.weedx.data.models.response.AssistantQueryResponse
import com.example.weedx.utils.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepository @Inject constructor(
    private val assistantApiService: AssistantApiService
) {
    suspend fun sendQuery(query: String): NetworkResult<AssistantQueryResponse> {
        return try {
            val request = mapOf("query" to query)
            val response = assistantApiService.sendQuery(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to send query")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getHistory(): NetworkResult<List<AssistantHistory>> {
        return try {
            val response = assistantApiService.getHistory()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch history")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
