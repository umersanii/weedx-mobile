package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.EnvironmentResponse
import com.example.weedx.data.models.response.CurrentWeather
import com.example.weedx.data.models.response.WeatherForecast
import com.example.weedx.data.models.response.SoilData
import retrofit2.Response
import retrofit2.http.GET

interface EnvironmentApiService {
    @GET("environment")
    suspend fun getEnvironment(): Response<ApiResponse<EnvironmentResponse>>
    
    @GET("environment/weather/current")
    suspend fun getCurrentWeather(): Response<ApiResponse<CurrentWeather>>
    
    @GET("environment/weather/forecast")
    suspend fun getWeatherForecast(): Response<ApiResponse<List<WeatherForecast>>>
    
    @GET("environment/soil")
    suspend fun getSoilData(): Response<ApiResponse<SoilData>>
    
    @GET("environment/recommendations/today")
    suspend fun getTodayRecommendations(): Response<ApiResponse<List<String>>>
}
