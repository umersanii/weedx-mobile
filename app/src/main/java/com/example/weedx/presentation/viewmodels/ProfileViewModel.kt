package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.models.response.FarmInfo
import com.example.weedx.data.models.response.ProfileResponse
import com.example.weedx.data.models.response.UserProfile
import com.example.weedx.data.repositories.ProfileRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState
    
    fun loadProfile() {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading
                
                when (val result = profileRepository.getProfile()) {
                    is NetworkResult.Success -> {
                        _profileState.value = ProfileState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        _profileState.value = ProfileState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _profileState.value = ProfileState.Loading
                    }
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    
    fun updateProfile(updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _updateState.value = UpdateState.Loading
                
                when (val result = profileRepository.updateProfile(updates)) {
                    is NetworkResult.Success -> {
                        _updateState.value = UpdateState.Success(result.data)
                        // Refresh profile after successful update
                        loadProfile()
                    }
                    is NetworkResult.Error -> {
                        _updateState.value = UpdateState.Error(result.message)
                    }
                    is NetworkResult.Loading -> {
                        _updateState.value = UpdateState.Loading
                    }
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    
    fun refresh() {
        loadProfile()
    }
    
    sealed class ProfileState {
        object Idle : ProfileState()
        object Loading : ProfileState()
        data class Success(val data: ProfileResponse) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }
    
    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        data class Success(val data: UserProfile) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
    
    companion object {
        // Default/dummy data to show when API fails
        val DEFAULT_USER = UserProfile(
            id = 0,
            name = "Bruce Wayne",
            email = "bruce.wayne@email.com",
            phone = null,
            avatarUrl = null,
            createdAt = "2024-01-01"
        )
        
        val DEFAULT_FARM = FarmInfo(
            id = 0,
            name = "Green Valley Farm",
            location = "Gotham City",
            area = 250.0,
            cropTypes = "Mixed"
        )
    }
}
