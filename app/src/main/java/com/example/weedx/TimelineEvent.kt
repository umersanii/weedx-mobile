package com.example.weedx

data class TimelineEvent(
    val title: String,
    val description: String,
    val timeAgo: String,
    val isLast: Boolean = false
)
