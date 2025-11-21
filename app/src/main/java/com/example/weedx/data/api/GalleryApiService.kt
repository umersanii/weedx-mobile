package com.example.weedx.data.api

import com.example.weedx.data.models.response.ApiResponse
import com.example.weedx.data.models.response.GalleryImage
import com.example.weedx.data.models.response.GalleryUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface GalleryApiService {
    @GET("gallery")
    suspend fun getGalleryImages(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<List<GalleryImage>>>
    
    @Multipart
    @POST("gallery")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("weed_type") weedType: String?,
        @Part("crop_type") cropType: String?
    ): Response<ApiResponse<GalleryUploadResponse>>
    
    @GET("gallery/{id}")
    suspend fun getImage(@Path("id") id: Int): Response<ApiResponse<GalleryImage>>
    
    @DELETE("gallery/{id}")
    suspend fun deleteImage(@Path("id") id: Int): Response<ApiResponse<String>>
}
