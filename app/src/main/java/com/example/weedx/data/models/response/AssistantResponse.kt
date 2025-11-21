package com.example.weedx.data.models.response

data class AssistantQueryResponse(
    val response: String,
    val timestamp: String,
    val conversationId: Int?
)

data class AssistantHistory(
    val id: Int,
    val query: String,
    val response: String,
    val timestamp: String
)
