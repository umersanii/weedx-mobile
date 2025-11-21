package com.example.weedx.data.models.response

data class LoginResponse(
    val token: String,
    val userId: String,
    val email: String
)
