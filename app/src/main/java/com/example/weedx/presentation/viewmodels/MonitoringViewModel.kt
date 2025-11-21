package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.Activity
import com.example.weedx.data.models.response.MonitoringResponse
import com.example.weedx.data.repositories.MonitoringRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val monitoringRepository: MonitoringRepository
) : ViewModel() {
    
    private val _monitoringState = MutableStateFlow<MonitoringState>(MonitoringState.Idle)
    val monitoringState: StateFlow<MonitoringState> = _monitoringState
    
    init {
        loadMonitoringData()
    }
    
    fun loadMonitoringData() {
        viewModelScope.launch {
            _monitoringState.value = MonitoringState.Loading
            
            when (val result = monitoringRepository.getMonitoring()) {
                is NetworkResult.Success -> {
                    _monitoringState.value = MonitoringState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _monitoringState.value = MonitoringState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _monitoringState.value = MonitoringState.Loading
                }
            }
        }
    }
    
    fun refresh() {
        loadMonitoringData()
    }
    
    sealed class MonitoringState {
        object Idle : MonitoringState()
        object Loading : MonitoringState()
        data class Success(val data: MonitoringResponse) : MonitoringState()
        data class Error(val message: String) : MonitoringState()
    }
}
