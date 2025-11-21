package com.example.weedx.data.models.response

data class ReportsResponse(
    val widgets: ReportWidgets,
    val weedTrend: List<TrendData>,
    val weedDistribution: List<DistributionData>
)

data class ReportWidgets(
    val totalWeeds: Int,
    val totalArea: Double,
    val totalHerbicide: Double,
    val efficiency: Double
)

data class TrendData(
    val date: String,
    val count: Int
)

data class DistributionData(
    val cropType: String,
    val weedType: String,
    val count: Int
)

data class ExportResponse(
    val url: String,
    val format: String,
    val filename: String
)
