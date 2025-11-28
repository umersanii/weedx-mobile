package com.example.weedx.data.models.response

data class RegisterResponse(
    val token: String,
    val userId: String,
    val email: String,
    val message: String? = null
)
