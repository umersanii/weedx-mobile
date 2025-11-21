package com.example.weedx.data.models.response

// Simple wrapper for all API responses
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)
