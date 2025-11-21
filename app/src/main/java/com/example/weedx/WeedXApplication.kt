package com.example.weedx

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt setup
 */
@HiltAndroidApp
class WeedXApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}
