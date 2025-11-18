package com.example.weedx

data class TrendData(
    val day: String,
    val value: Int,
    val maxValue: Int = 250
) {
    val progress: Int
        get() = ((value.toFloat() / maxValue) * 100).toInt()
}
