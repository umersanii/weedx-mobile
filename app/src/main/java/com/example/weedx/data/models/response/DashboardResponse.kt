package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("robot_status")
    val robotStatus: RobotStatus,
    @SerializedName("todays_summary")
    val todaySummary: TodaySummary,
    @SerializedName("recent_alerts")
    val recentAlerts: List<Alert>
)

data class RobotStatus(
    val battery: Int,
    val latitude: Double?,
    val longitude: Double?,
    val speed: Double?,
    val status: String,
    @SerializedName("last_updated")
    val lastUpdate: String?
)

data class TodaySummary(
    @SerializedName("weeds_detected")
    val weedsDetected: Int,
    @SerializedName("area_covered")
    val areaCovered: Double,
    @SerializedName("herbicide_used")
    val herbicideUsed: Double,
    @SerializedName("session_duration")
    val sessionDuration: Int?
)

data class Alert(
    val id: Int,
    val type: String,
    val message: String,
    val severity: String,
    val timestamp: String,
    @SerializedName("is_read")
    val isRead: Boolean
)
