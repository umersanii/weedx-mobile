package com.example.weedx.utils

import com.example.weedx.BuildConfig

object Constants {
    // Backend API endpoints are configured through Gradle build config fields.
    const val BASE_URL: String = BuildConfig.BACKEND_BASE_URL
    const val IMAGE_BASE_URL: String = BuildConfig.IMAGE_BASE_URL

    /**
     * Constructs a full image URL from a path.
     * If the path is already a full URL, returns it as-is.
     * If it's a relative path, prepends the IMAGE_BASE_URL.
     */
    fun getFullImageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("/") -> IMAGE_BASE_URL + path
            else -> "$IMAGE_BASE_URL/$path"
        }
    }
    
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
