package com.example.weedx.data.models.response

data class EnvironmentResponse(
    val currentWeather: CurrentWeather,
    val forecast: List<WeatherForecast>,
    val soil: SoilData,
    val recommendations: List<String>
)

data class CurrentWeather(
    val temperature: Double,
    val humidity: Int,
    val weatherCondition: String,
    val windSpeed: Double?,
    val precipitation: Double?,
    val timestamp: String
)

data class WeatherForecast(
    val date: String,
    val temperature: Double,
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
    val timestamp: String
)
