package com.example.weedx

data class ForecastDay(
    val day: String,
    val description: String,
    val icon: Int,
    val maxTemp: Int,
    val minTemp: Int
)
