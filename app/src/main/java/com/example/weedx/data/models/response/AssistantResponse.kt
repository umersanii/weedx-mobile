package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class AssistantQueryResponse(
    val response: String,
    val timestamp: String,
    @SerializedName("conversationId")
    val conversationId: Int? = null
)

data class AssistantHistory(
    val id: Int,
    val message: String,
    @SerializedName("is_user")
    val isUser: Boolean,
    val timestamp: String
)
