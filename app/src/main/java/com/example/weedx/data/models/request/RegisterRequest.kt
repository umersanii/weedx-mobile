package com.example.weedx.data.models.request

data class RegisterRequest(
    // User Information
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    
    // Farm Information
    val farmName: String,
    val farmLocation: String,
    val farmSize: Double,
    val cropTypes: List<String>? = null,
    
    // App Settings
    val notificationsEnabled: Boolean = true,
    val emailAlerts: Boolean = true,
    val language: String = "en",
    val theme: String = "light",
    
    // Firebase token
    val firebaseToken: String = "demo_token"
)
