package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class WeedLogsResponse(
    val summary: List<WeedSummary>,
    val detections: List<WeedDetection>
)

data class WeedSummary(
    @SerializedName("weed_type")
    val weedType: String,
    val count: Int
)

data class WeedDetection(
    val id: Int,
    @SerializedName("weed_type")
    val weedType: String,
    @SerializedName("crop_type")
    val cropType: String?,
    val confidence: Double,
    val location: WeedLocation?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("detected_at")
    val detectedAt: String,
    val action: String?
)

data class WeedLocation(
    val latitude: Double?,
    val longitude: Double?
)
