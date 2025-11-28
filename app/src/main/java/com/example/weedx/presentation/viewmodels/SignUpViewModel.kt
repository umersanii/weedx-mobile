package com.example.weedx.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedx.data.repositories.AuthRepository
import com.example.weedx.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState
    
    // Form data holders
    var name: String = ""
    var email: String = ""
    var password: String = ""
    var confirmPassword: String = ""
    var phone: String = ""
    
    var farmName: String = ""
    var farmLocation: String = ""
    var farmSize: String = ""
    var selectedCropTypes: MutableList<String> = mutableListOf()
    
    var notificationsEnabled: Boolean = true
    var emailAlerts: Boolean = true
    var selectedLanguage: String = "en"
    var selectedTheme: String = "light"
    
    fun register() {
        // Validate inputs
        val validationError = validateInputs()
        if (validationError != null) {
            _signUpState.value = SignUpState.Error(validationError)
            return
        }
        
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            
            val result = authRepository.register(
                name = name,
                email = email,
                password = password,
                phone = phone.ifBlank { null },
                farmName = farmName,
                farmLocation = farmLocation,
                farmSize = farmSize.toDoubleOrNull() ?: 0.0,
                cropTypes = selectedCropTypes.ifEmpty { null },
                notificationsEnabled = notificationsEnabled,
                emailAlerts = emailAlerts,
                language = selectedLanguage,
                theme = selectedTheme
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    _signUpState.value = SignUpState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _signUpState.value = SignUpState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _signUpState.value = SignUpState.Loading
                }
            }
        }
    }
    
    private fun validateInputs(): String? {
        if (name.isBlank()) return "Name is required"
        if (email.isBlank()) return "Email is required"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email format"
        }
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        if (password != confirmPassword) return "Passwords do not match"
        if (farmName.isBlank()) return "Farm name is required"
        if (farmLocation.isBlank()) return "Farm location is required"
        if (farmSize.isBlank()) return "Farm size is required"
        if (farmSize.toDoubleOrNull() == null || farmSize.toDouble() <= 0) {
            return "Invalid farm size"
        }
        return null
    }
    
    fun resetState() {
        _signUpState.value = SignUpState.Idle
    }
    
    sealed class SignUpState {
        object Idle : SignUpState()
        object Loading : SignUpState()
        data class Success(val message: String) : SignUpState()
        data class Error(val message: String) : SignUpState()
    }
}
