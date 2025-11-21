package com.example.weedx.utils

object Constants {
    // PHP Backend URL - update with your local IP or production server
    // Local: http://192.168.1.8/weedx-backend/
    // For emulator: http://10.0.2.2/weedx-backend/
    const val BASE_URL = "http://192.168.1.8/weedx-backend/"
    
    // SharedPreferences
    const val PREFS_NAME = "weedx_prefs"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    
    // Network
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
