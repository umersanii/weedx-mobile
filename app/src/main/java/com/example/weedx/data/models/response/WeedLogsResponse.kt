package com.example.weedx.data.models.response

data class WeedLogsResponse(
    val summary: List<WeedSummary>,
    val detections: List<WeedDetection>
)

data class WeedSummary(
    val weedType: String,
    val count: Int
)

data class WeedDetection(
    val id: Int,
    val weedType: String,
    val cropType: String?,
    val confidence: Double,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?,
    val detectedAt: String,
    val action: String?
)
