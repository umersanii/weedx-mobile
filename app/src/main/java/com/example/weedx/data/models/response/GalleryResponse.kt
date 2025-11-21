package com.example.weedx.data.models.response

data class GalleryImage(
    val id: Int,
    val url: String,
    val thumbnailUrl: String?,
    val weedType: String,
    val cropType: String?,
    val detectionId: Int?,
    val uploadedAt: String
)

data class GalleryUploadResponse(
    val id: Int,
    val url: String,
    val message: String
)
