package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class MonitoringResponse(
    val metrics: Metrics,
    @SerializedName("activity_timeline")
    val activityTimeline: List<Activity>?,
    val location: Location?
)

data class Metrics(
    val battery: Int,
    @SerializedName("herbicide_level")
    val herbicideLevel: Double,
    val coverage: Double,
    val efficiency: Double
)

data class Activity(
    val id: Int,
    val action: String,
    val timestamp: String,
    val description: String?
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double? = null
)
