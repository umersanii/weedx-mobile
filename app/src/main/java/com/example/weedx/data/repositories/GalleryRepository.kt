package com.example.weedx.data.repositories

import com.example.weedx.data.api.GalleryApiService
import com.example.weedx.data.models.response.GalleryImage
import com.example.weedx.data.models.response.GalleryUploadResponse
import com.example.weedx.utils.NetworkResult
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    private val galleryApiService: GalleryApiService
) {
    suspend fun getGalleryImages(offset: Int? = null, limit: Int? = null): NetworkResult<List<GalleryImage>> {
        return try {
            val response = galleryApiService.getGalleryImages(offset, limit)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch gallery")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun uploadImage(
        image: MultipartBody.Part,
        weedType: String?,
        cropType: String?
    ): NetworkResult<GalleryUploadResponse> {
        return try {
            val response = galleryApiService.uploadImage(image, weedType, cropType)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to upload image")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun getImage(id: Int): NetworkResult<GalleryImage> {
        return try {
            val response = galleryApiService.getImage(id)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to fetch image")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    suspend fun deleteImage(id: Int): NetworkResult<String> {
        return try {
            val response = galleryApiService.deleteImage(id)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Success("Image deleted successfully")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to delete image")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
