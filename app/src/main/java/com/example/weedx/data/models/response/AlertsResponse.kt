package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class AlertsResponse(
    val alerts: List<Alert>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val total: Int,
    val page: Int,
    val limit: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)
