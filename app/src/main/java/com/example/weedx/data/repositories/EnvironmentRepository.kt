package com.example.weedx.data.repositories

import com.example.weedx.data.api.EnvironmentApiService
import com.example.weedx.data.models.response.CurrentWeather
import com.example.weedx.data.models.response.EnvironmentResponse
import com.example.weedx.data.models.response.SoilData
import com.example.weedx.data.models.response.WeatherForecast
import com.example.weedx.utils.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentRepository @Inject constructor(
    private val environmentApiService: EnvironmentApiService
) {
    suspend fun getEnvironment(): NetworkResult<EnvironmentResponse> {
        return try {
            val response = environmentApiService.getEnvironment()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch environment data")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getCurrentWeather(): NetworkResult<CurrentWeather> {
        return try {
            val response = environmentApiService.getCurrentWeather()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch weather")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getWeatherForecast(): NetworkResult<List<WeatherForecast>> {
        return try {
            val response = environmentApiService.getWeatherForecast()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch forecast")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getSoilData(): NetworkResult<SoilData> {
        return try {
            val response = environmentApiService.getSoilData()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch soil data")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getTodayRecommendations(): NetworkResult<List<String>> {
        return try {
            val response = environmentApiService.getTodayRecommendations()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch recommendations")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
