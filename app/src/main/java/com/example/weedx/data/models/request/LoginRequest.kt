package com.example.weedx.data.models.request

data class LoginRequest(
    val email: String,
    val password: String,
    val firebaseToken: String = "demo_token"
)
