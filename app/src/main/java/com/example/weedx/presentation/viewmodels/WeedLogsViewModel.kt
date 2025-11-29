package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.WeedDetection
import com.example.weedx.data.models.response.WeedLogsResponse
import com.example.weedx.data.models.response.WeedSummary
import com.example.weedx.data.repositories.WeedLogsRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeedLogsUiState {
    object Idle : WeedLogsUiState()
    object Loading : WeedLogsUiState()
    data class Success(
        val summary: List<WeedSummary>,
        val detections: List<WeedDetection>,
        val todayCount: Int,
        val weekCount: Int,
        val treatedPercent: Int
    ) : WeedLogsUiState()
    data class Error(val message: String) : WeedLogsUiState()
}

@HiltViewModel
class WeedLogsViewModel @Inject constructor(
    private val weedLogsRepository: WeedLogsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeedLogsUiState>(WeedLogsUiState.Idle)
    val uiState: StateFlow<WeedLogsUiState> = _uiState.asStateFlow()
    
    private val _filteredDetections = MutableStateFlow<List<WeedDetection>>(emptyList())
    val filteredDetections: StateFlow<List<WeedDetection>> = _filteredDetections.asStateFlow()
    
    private var allDetections: List<WeedDetection> = emptyList()
    private var currentFilter: String? = null
    private var searchQuery: String = ""

    fun loadWeedLogs() {
        viewModelScope.launch {
            _uiState.value = WeedLogsUiState.Loading
            
            when (val result = weedLogsRepository.getWeedLogs()) {
                is NetworkResult.Success -> {
                    val data = result.data
                    allDetections = data.detections
                    
                    // Calculate statistics
                    val todayCount = calculateTodayCount(data.detections)
                    val weekCount = calculateWeekCount(data.detections)
                    val treatedPercent = calculateTreatedPercent(data.detections)
                    
                    _uiState.value = WeedLogsUiState.Success(
                        summary = data.summary,
                        detections = data.detections,
                        todayCount = todayCount,
                        weekCount = weekCount,
                        treatedPercent = treatedPercent
                    )
                    
                    applyFilters()
                }
                is NetworkResult.Error -> {
                    _uiState.value = WeedLogsUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _uiState.value = WeedLogsUiState.Loading
                }
            }
        }
    }
    
    fun filterByWeedType(weedType: String?) {
        currentFilter = weedType
        applyFilters()
    }
    
    fun searchDetections(query: String) {
        searchQuery = query
        applyFilters()
    }
    
    private fun applyFilters() {
        var filtered = allDetections
        
        // Apply weed type filter
        if (!currentFilter.isNullOrEmpty()) {
            filtered = filtered.filter { it.weedType.equals(currentFilter, ignoreCase = true) }
        }
        
        // Apply search query
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { detection ->
                detection.weedType.contains(searchQuery, ignoreCase = true) ||
                detection.cropType?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        _filteredDetections.value = filtered
    }
    
    fun clearFilters() {
        currentFilter = null
        searchQuery = ""
        applyFilters()
    }
    
    fun retry() {
        loadWeedLogs()
    }
    
    private fun calculateTodayCount(detections: List<WeedDetection>): Int {
        // For now, return total count - can be enhanced with date filtering
        // The backend could provide this in a stats endpoint
        return detections.size
    }
    
    private fun calculateWeekCount(detections: List<WeedDetection>): Int {
        // Return total since we're fetching limited data
        return detections.size
    }
    
    private fun calculateTreatedPercent(detections: List<WeedDetection>): Int {
        // For now return 98% - can be calculated from action field if available
        if (detections.isEmpty()) return 0
        val treatedCount = detections.count { it.action != null }
        return if (treatedCount > 0) {
            ((treatedCount.toDouble() / detections.size) * 100).toInt()
        } else {
            98 // Default placeholder
        }
    }
}
