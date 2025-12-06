package com.example.weedx.data.api

import com.example.weedx.data.models.request.FcmTokenRequest
import com.example.weedx.data.models.response.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface FcmTokenApiService {
    
    @POST("profile/fcm-token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest
    ): Response<ApiResponse<Unit>>
    
    @DELETE("profile/fcm-token")
    suspend fun deactivateFcmToken(
        @Body request: FcmTokenRequest
    ): Response<ApiResponse<Unit>>
}
