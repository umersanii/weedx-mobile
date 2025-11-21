package com.example.weedx.data.repositories

import android.content.SharedPreferences
import com.example.weedx.data.api.AuthApiService
import com.example.weedx.data.models.request.LoginRequest
import com.example.weedx.utils.Constants
import com.example.weedx.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val firebaseAuth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
) {
    suspend fun login(email: String, password: String): NetworkResult<String> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val response = authApiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { data ->
                    sharedPreferences.edit()
                        .putString(Constants.KEY_AUTH_TOKEN, data.token)
                        .putString(Constants.KEY_USER_ID, data.userId)
                        .putString(Constants.KEY_USER_EMAIL, data.email)
                        .apply()
                    NetworkResult.Success("Login successful")
                } ?: NetworkResult.Error("No data received")
            } else {
                NetworkResult.Error(response.body()?.message ?: "Login failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    
    fun logout() {
        firebaseAuth.signOut()
        sharedPreferences.edit().clear().apply()
    }
}
