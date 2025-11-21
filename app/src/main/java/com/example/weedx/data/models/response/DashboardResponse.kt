package com.example.weedx.data.models.response

data class DashboardResponse(
    val robotStatus: RobotStatus,
    val todaySummary: TodaySummary,
    val recentAlerts: List<Alert>
)

data class RobotStatus(
    val battery: Int,
    val latitude: Double?,
    val longitude: Double?,
    val speed: Double?,
    val status: String,
    val lastUpdate: String?
)

data class TodaySummary(
    val weedsDetected: Int,
    val areaCovered: Double,
    val herbicideUsed: Double,
    val sessionDuration: Int?
)

data class Alert(
    val id: Int,
    val type: String,
    val message: String,
    val severity: String,
    val timestamp: String,
    val isRead: Boolean
)
