package com.example.weedx.utils

object Constants {
    // PHP Backend URL - Tailscale network
    // Raspberry Pi: http://raspberrypi.mullet-bull.ts.net/weedx-backend/
    // Local fallback: http://192.168.1.8/weedx-backend/
    const val BASE_URL = "http://raspberrypi.mullet-bull.ts.net/weedx-backend/"
    
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
