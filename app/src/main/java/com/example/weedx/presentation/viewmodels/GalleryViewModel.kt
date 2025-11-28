 package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.GalleryImage
import com.example.weedx.data.repositories.GalleryRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(val images: List<GalleryImage>) : GalleryUiState()
    data class Error(val message: String) : GalleryUiState()
}

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadGalleryImages()
    }

    fun loadGalleryImages(offset: Int? = null, limit: Int? = null) {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            when (val result = galleryRepository.getGalleryImages(offset, limit)) {
                is NetworkResult.Success -> {
                    _uiState.value = GalleryUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = GalleryUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _uiState.value = GalleryUiState.Loading
                }
            }
        }
    }

    fun deleteImage(id: Int) {
        viewModelScope.launch {
            when (val result = galleryRepository.deleteImage(id)) {
                is NetworkResult.Success -> {
                    // Reload images after deletion
                    loadGalleryImages()
                }
                is NetworkResult.Error -> {
                    _uiState.value = GalleryUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    fun retry() {
        loadGalleryImages()
    }
}
