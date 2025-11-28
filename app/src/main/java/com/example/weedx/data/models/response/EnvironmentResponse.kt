package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class EnvironmentResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather,
    val forecast: List<WeatherForecast>,
    val soil: SoilData,
    val recommendations: List<String>
)

data class CurrentWeather(
    val temperature: Double,
    val humidity: Int,
    @SerializedName("condition")
    val weatherCondition: String,
    @SerializedName("wind_speed")
    val windSpeed: Double?,
    val precipitation: Double?,
    @SerializedName("uv_index")
    val uvIndex: Int?,
    @SerializedName("recorded_at")
    val timestamp: String?
)

data class WeatherForecast(
    val date: String,
    @SerializedName("temp_high")
    val tempHigh: Double,
    @SerializedName("temp_low")
    val tempLow: Double,
    @SerializedName("condition")
    val weatherCondition: String,
    val precipitation: Double?,
    val humidity: Int?
)

data class SoilData(
    val temperature: Double,
    val moisture: Double,
    val ph: Double,
    val nitrogen: Double?,
    val phosphorus: Double?,
    val potassium: Double?,
    @SerializedName("recorded_at")
    val timestamp: String?
)
