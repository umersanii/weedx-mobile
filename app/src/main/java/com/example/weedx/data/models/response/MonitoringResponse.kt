package com.example.weedx.data.models.response

data class MonitoringResponse(
    val metrics: Metrics,
    val activityTimeline: List<Activity>,
    val location: Location?
)

data class Metrics(
    val battery: Int,
    val herbicideLevel: Double,
    val areaCovered: Double,
    val efficiency: Double
)

data class Activity(
    val id: Int,
    val action: String,
    val timestamp: String,
    val details: String?
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double?
)
