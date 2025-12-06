package com.example.weedx.data.repositories

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.weedx.data.api.FcmTokenApiService
import com.example.weedx.data.models.request.FcmTokenRequest
import com.example.weedx.utils.NetworkResult
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenRepository @Inject constructor(
    private val fcmTokenApiService: FcmTokenApiService,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FcmTokenRepository"
        private const val PREFS_NAME = "weedx_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_TOKEN_SENT = "fcm_token_sent"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get FCM token and register it with backend
     */
    suspend fun registerFcmToken(): NetworkResult<Unit> {
        return try {
            // Get FCM token from Firebase
            val token = FirebaseMessaging.getInstance().token.await()
            
            Log.d(TAG, "FCM Token obtained: $token")
            
            // Check if token is already sent
            val lastSentToken = prefs.getString(KEY_FCM_TOKEN, null)
            val tokenSent = prefs.getBoolean(KEY_TOKEN_SENT, false)
            
            if (token == lastSentToken && tokenSent) {
                Log.d(TAG, "FCM token already registered")
                return NetworkResult.Success(Unit)
            }
            
            // Get device info
            val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
            
            // Send token to backend
            val request = FcmTokenRequest(token, deviceInfo)
            val response = fcmTokenApiService.registerFcmToken(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Save token to preferences
                prefs.edit()
                    .putString(KEY_FCM_TOKEN, token)
                    .putBoolean(KEY_TOKEN_SENT, true)
                    .apply()
                
                Log.d(TAG, "FCM token registered successfully")
                NetworkResult.Success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to register FCM token"
                Log.e(TAG, errorMessage)
                NetworkResult.Error(errorMessage)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error registering FCM token", e)
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Deactivate FCM token (on logout)
     */
    suspend fun deactivateFcmToken(): NetworkResult<Unit> {
        return try {
            val token = prefs.getString(KEY_FCM_TOKEN, null)
            
            if (token.isNullOrEmpty()) {
                Log.d(TAG, "No FCM token to deactivate")
                return NetworkResult.Success(Unit)
            }
            
            val request = FcmTokenRequest(token)
            val response = fcmTokenApiService.deactivateFcmToken(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Clear token from preferences
                prefs.edit()
                    .remove(KEY_FCM_TOKEN)
                    .putBoolean(KEY_TOKEN_SENT, false)
                    .apply()
                
                Log.d(TAG, "FCM token deactivated successfully")
                NetworkResult.Success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to deactivate FCM token"
                Log.e(TAG, errorMessage)
                NetworkResult.Error(errorMessage)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating FCM token", e)
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Check if FCM token needs to be sent to backend
     */
    fun needsTokenRegistration(): Boolean {
        return !prefs.getBoolean(KEY_TOKEN_SENT, false)
    }
}
