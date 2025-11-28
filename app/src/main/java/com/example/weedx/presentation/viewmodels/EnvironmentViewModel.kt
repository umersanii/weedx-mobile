package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.CurrentWeather
import com.example.weedx.data.models.response.EnvironmentResponse
import com.example.weedx.data.models.response.SoilData
import com.example.weedx.data.models.response.WeatherForecast
import com.example.weedx.data.repositories.EnvironmentRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnvironmentViewModel @Inject constructor(
    private val environmentRepository: EnvironmentRepository
) : ViewModel() {

    private val _environmentState = MutableStateFlow<EnvironmentState>(EnvironmentState.Idle)
    val environmentState: StateFlow<EnvironmentState> = _environmentState

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState

    private val _forecastState = MutableStateFlow<ForecastState>(ForecastState.Idle)
    val forecastState: StateFlow<ForecastState> = _forecastState

    private val _soilState = MutableStateFlow<SoilState>(SoilState.Idle)
    val soilState: StateFlow<SoilState> = _soilState

    fun loadAllEnvironmentData() {
        loadEnvironment()
    }

    fun loadEnvironment() {
        viewModelScope.launch {
            try {
                _environmentState.value = EnvironmentState.Loading
                _weatherState.value = WeatherState.Loading
                _forecastState.value = ForecastState.Loading
                _soilState.value = SoilState.Loading

                when (val result = environmentRepository.getEnvironment()) {
                    is NetworkResult.Success -> {
                        val data = result.data
                        _environmentState.value = EnvironmentState.Success(data)
                        _weatherState.value = WeatherState.Success(data.currentWeather)
                        _forecastState.value = ForecastState.Success(data.forecast)
                        _soilState.value = SoilState.Success(data.soil)
                    }
                    is NetworkResult.Error -> {
                        _environmentState.value = EnvironmentState.Error(result.message)
                        _weatherState.value = WeatherState.Error(result.message)
                        _forecastState.value = ForecastState.Error(result.message)
                        _soilState.value = SoilState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _environmentState.value = EnvironmentState.Loading
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Unknown error"
                _environmentState.value = EnvironmentState.Error(errorMsg)
                _weatherState.value = WeatherState.Error(errorMsg)
                _forecastState.value = ForecastState.Error(errorMsg)
                _soilState.value = SoilState.Error(errorMsg)
            }
        }
    }

    fun loadCurrentWeather() {
        viewModelScope.launch {
            try {
                _weatherState.value = WeatherState.Loading

                when (val result = environmentRepository.getCurrentWeather()) {
                    is NetworkResult.Success -> {
                        _weatherState.value = WeatherState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        _weatherState.value = WeatherState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _weatherState.value = WeatherState.Loading
                    }
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadForecast() {
        viewModelScope.launch {
            try {
                _forecastState.value = ForecastState.Loading

                when (val result = environmentRepository.getWeatherForecast()) {
                    is NetworkResult.Success -> {
                        _forecastState.value = ForecastState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        _forecastState.value = ForecastState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _forecastState.value = ForecastState.Loading
                    }
                }
            } catch (e: Exception) {
                _forecastState.value = ForecastState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadSoilData() {
        viewModelScope.launch {
            try {
                _soilState.value = SoilState.Loading

                when (val result = environmentRepository.getSoilData()) {
                    is NetworkResult.Success -> {
                        _soilState.value = SoilState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        _soilState.value = SoilState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _soilState.value = SoilState.Loading
                    }
                }
            } catch (e: Exception) {
                _soilState.value = SoilState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadAllEnvironmentData()
    }

    sealed class EnvironmentState {
        object Idle : EnvironmentState()
        object Loading : EnvironmentState()
        data class Success(val data: EnvironmentResponse) : EnvironmentState()
        data class Error(val message: String) : EnvironmentState()
    }

    sealed class WeatherState {
        object Idle : WeatherState()
        object Loading : WeatherState()
        data class Success(val weather: CurrentWeather) : WeatherState()
        data class Error(val message: String) : WeatherState()
    }

    sealed class ForecastState {
        object Idle : ForecastState()
        object Loading : ForecastState()
        data class Success(val forecast: List<WeatherForecast>) : ForecastState()
        data class Error(val message: String) : ForecastState()
    }

    sealed class SoilState {
        object Idle : SoilState()
        object Loading : SoilState()
        data class Success(val soil: SoilData) : SoilState()
        data class Error(val message: String) : SoilState()
    }
}
