package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.DistributionData
import com.example.weedx.data.models.response.ExportResponse
import com.example.weedx.data.models.response.ReportWidgets
import com.example.weedx.data.models.response.ReportsResponse
import com.example.weedx.data.models.response.TrendData
import com.example.weedx.data.repositories.ReportsRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ReportsState>(ReportsState.Idle)
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    private val _trendState = MutableStateFlow<TrendState>(TrendState.Idle)
    val trendState: StateFlow<TrendState> = _trendState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(Period.WEEKLY)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    // Cached data
    private var cachedReportsData: ReportsResponse? = null

    sealed class ReportsState {
        data object Idle : ReportsState()
        data object Loading : ReportsState()
        data class Success(val data: ReportsResponse) : ReportsState()
        data class Error(val message: String) : ReportsState()
    }

    sealed class TrendState {
        data object Idle : TrendState()
        data object Loading : TrendState()
        data class Success(val data: List<TrendData>) : TrendState()
        data class Error(val message: String) : TrendState()
    }

    sealed class ExportState {
        data object Idle : ExportState()
        data object Loading : ExportState()
        data class Success(val data: ExportResponse) : ExportState()
        data class Error(val message: String) : ExportState()
    }

    enum class Period(val days: Int) {
        WEEKLY(7),
        MONTHLY(30)
    }

    fun loadReports() {
        viewModelScope.launch {
            _state.value = ReportsState.Loading

            when (val result = reportsRepository.getReports()) {
                is NetworkResult.Success -> {
                    cachedReportsData = result.data
                    _state.value = ReportsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _state.value = ReportsState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _state.value = ReportsState.Loading
                }
            }
        }
    }

    fun loadTrend(days: Int? = null) {
        viewModelScope.launch {
            _trendState.value = TrendState.Loading

            val daysToFetch = days ?: _selectedPeriod.value.days
            when (val result = reportsRepository.getWeedTrend(daysToFetch)) {
                is NetworkResult.Success -> {
                    _trendState.value = TrendState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _trendState.value = TrendState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _trendState.value = TrendState.Loading
                }
            }
        }
    }

    fun selectPeriod(period: Period) {
        if (_selectedPeriod.value != period) {
            _selectedPeriod.value = period
            loadTrend(period.days)
        }
    }

    fun exportReport(format: String = "csv") {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            when (val result = reportsRepository.exportReport(format)) {
                is NetworkResult.Success -> {
                    _exportState.value = ExportState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _exportState.value = ExportState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _exportState.value = ExportState.Loading
                }
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    fun refresh() {
        loadReports()
        loadTrend()
    }

    // Helper methods for distribution chart
    fun getDistributionByWeedType(): Map<String, Int> {
        val currentState = _state.value
        if (currentState !is ReportsState.Success) return emptyMap()

        return currentState.data.weedDistribution
            .groupBy { it.weedType }
            .mapValues { (_, items) -> items.sumOf { it.count } }
    }

    fun getDistributionByCropType(): Map<String, List<DistributionData>> {
        val currentState = _state.value
        if (currentState !is ReportsState.Success) return emptyMap()

        return currentState.data.weedDistribution.groupBy { it.cropType ?: "Unknown" }
    }

    // Format helpers
    fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    fun formatArea(area: Double): String {
        return when {
            area >= 100 -> String.format("%.0fha", area)
            area >= 10 -> String.format("%.1fha", area)
            else -> String.format("%.2fha", area)
        }
    }

    fun formatHerbicide(liters: Double): String {
        return String.format("%.1fL", liters)
    }

    fun formatEfficiency(efficiency: Double): String {
        return String.format("%.0f%%", efficiency)
    }
}
