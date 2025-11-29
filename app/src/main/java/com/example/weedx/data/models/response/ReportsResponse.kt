package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class ReportsResponse(
    @SerializedName("widgets")
    val widgets: ReportWidgets,
    
    @SerializedName("weed_trend")
    val weedTrend: List<TrendData>,
    
    @SerializedName("weed_distribution")
    val weedDistribution: List<DistributionData>
)

data class ReportWidgets(
    @SerializedName("total_weeds")
    val totalWeeds: Int,
    
    @SerializedName("area_covered")
    val areaCovered: Double,
    
    @SerializedName("herbicide_used")
    val herbicideUsed: Double,
    
    @SerializedName("efficiency")
    val efficiency: Double
)

data class TrendData(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("count")
    val count: Int
)

data class DistributionData(
    @SerializedName("crop_type")
    val cropType: String?,
    
    @SerializedName("weed_type")
    val weedType: String,
    
    @SerializedName("count")
    val count: Int
)

data class ExportResponse(
    @SerializedName("url")
    val url: String,
    
    @SerializedName("format")
    val format: String,
    
    @SerializedName("filename")
    val filename: String
)
