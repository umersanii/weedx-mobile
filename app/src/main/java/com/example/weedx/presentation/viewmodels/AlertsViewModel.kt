package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.Alert
import com.example.weedx.data.models.response.AlertsResponse
import com.example.weedx.data.repositories.AlertsRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertsRepository: AlertsRepository
) : ViewModel() {

    private val _alertsState = MutableStateFlow<AlertsState>(AlertsState.Idle)
    val alertsState: StateFlow<AlertsState> = _alertsState

    private var currentPage = 1
    private var canLoadMore = true

    fun loadAlerts(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            canLoadMore = true
        }

        if (!canLoadMore && !refresh) return

        viewModelScope.launch {
            try {
                _alertsState.value = if (refresh) AlertsState.Loading else AlertsState.LoadingMore
                
                when (val result = alertsRepository.getAllAlerts(currentPage, 50)) {
                    is NetworkResult.Success -> {
                        val response = result.data
                        _alertsState.value = AlertsState.Success(
                            alerts = response.alerts,
                            hasMore = currentPage < response.pagination.totalPages,
                            totalCount = response.pagination.total
                        )
                        canLoadMore = currentPage < response.pagination.totalPages
                        currentPage++
                    }
                    is NetworkResult.Error -> {
                        _alertsState.value = AlertsState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        // Already handled above
                    }
                }
            } catch (e: Exception) {
                _alertsState.value = AlertsState.Error(e.message ?: "An error occurred")
            }
        }
    }

    sealed class AlertsState {
        object Idle : AlertsState()
        object Loading : AlertsState()
        object LoadingMore : AlertsState()
        data class Success(
            val alerts: List<Alert>,
            val hasMore: Boolean,
            val totalCount: Int
        ) : AlertsState()
        data class Error(val message: String) : AlertsState()
    }
}
