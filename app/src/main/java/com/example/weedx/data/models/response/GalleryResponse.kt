package com.example.weedx.data.models.response

import com.google.gson.annotations.SerializedName

data class GalleryImage(
    val id: Int,
    val url: String,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    @SerializedName("weed_type")
    val weedType: String,
    val confidence: Float? = null,
    val location: Location? = null,
    @SerializedName("crop_type")
    val cropType: String? = null,
    @SerializedName("detection_id")
    val detectionId: Int? = null,
    @SerializedName("captured_at")
    val capturedAt: String? = null,
    @SerializedName("uploaded_at")
    val uploadedAt: String? = null
)



data class GalleryUploadResponse(
    val id: Int,
    val url: String,
    val message: String? = null,
    @SerializedName("uploaded_at")
    val uploadedAt: String? = null
)
