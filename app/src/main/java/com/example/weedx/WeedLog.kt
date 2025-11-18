package com.example.weedx

data class WeedLog(
    val name: String,
    val weedsCount: Int,
    val zone: String,
    val timeAgo: String,
    val isTreated: Boolean = true
)
