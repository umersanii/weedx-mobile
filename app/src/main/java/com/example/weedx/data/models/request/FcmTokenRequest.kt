package com.example.weedx.data.models.request

data class FcmTokenRequest(
    val token: String,
    val device_info: String? = null
)
