package com.example.weedx.data.api

import com.example.weedx.data.models.request.LoginRequest
import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
}
