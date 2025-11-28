package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.Alert
import com.example.weedx.data.models.response.CurrentWeather
import com.example.weedx.data.models.response.DashboardResponse
import com.example.weedx.data.repositories.DashboardRepository
import com.example.weedx.data.repositories.EnvironmentRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val environmentRepository: EnvironmentRepository
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState
    
    private val _alertsState = MutableStateFlow<AlertsState>(AlertsState.Idle)
    val alertsState: StateFlow<AlertsState> = _alertsState
    
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState
    
    init {
        // Don't auto-load on init to prevent crashes during activity creation
        // Let the activity trigger the load explicitly
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _dashboardState.value = DashboardState.Loading
                
                when (val result = dashboardRepository.getDashboard()) {
                    is NetworkResult.Success -> {
                        _dashboardState.value = DashboardState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        _dashboardState.value = DashboardState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _dashboardState.value = DashboardState.Loading
                    }
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    
    fun loadAlerts() {
        viewModelScope.launch {
            try {
                _alertsState.value = AlertsState.Loading
                
                when (val result = dashboardRepository.getRecentAlerts()) {
                    is NetworkResult.Success -> {
                        _alertsState.value = AlertsState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        _alertsState.value = AlertsState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _alertsState.value = AlertsState.Loading
                    }
                }
            } catch (e: Exception) {
                _alertsState.value = AlertsState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    
    fun loadWeather() {
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
    
    fun refresh() {
        loadDashboardData()
        loadAlerts()
        loadWeather()
    }
    
    sealed class DashboardState {
        object Idle : DashboardState()
        object Loading : DashboardState()
        data class Success(val data: DashboardResponse) : DashboardState()
        data class Error(val message: String) : DashboardState()
    }
    
    sealed class AlertsState {
        object Idle : AlertsState()
        object Loading : AlertsState()
        data class Success(val alerts: List<Alert>) : AlertsState()
        data class Error(val message: String) : AlertsState()
    }
    
    sealed class WeatherState {
        object Idle : WeatherState()
        object Loading : WeatherState()
        data class Success(val weather: CurrentWeather) : WeatherState()
        data class Error(val message: String) : WeatherState()
    }
}
