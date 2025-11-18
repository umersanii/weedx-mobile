package com.example.weedx

data class SoilMetric(
    val name: String,
    val value: String,
    val icon: Int,
    val status: String? = null
)
