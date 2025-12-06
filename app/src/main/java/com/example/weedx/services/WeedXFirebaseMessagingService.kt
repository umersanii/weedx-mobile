package com.example.weedx.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.weedx.DashboardActivity
import com.example.weedx.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications and FCM token updates
 */
@AndroidEntryPoint
class WeedXFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "WeedXFCMService"
        private const val CHANNEL_ID = "weedx_alerts"
        private const val CHANNEL_NAME = "WeedX Alerts"
        private const val CHANNEL_DESCRIPTION = "Notifications for robot alerts and system updates"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Send token to backend
        sendTokenToBackend(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        // Handle notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "WeedX Alert"
            val body = notification.body ?: ""
            
            showNotification(title, body, message.data)
        }
        
        // Handle data payload (if notification is not present)
        if (message.notification == null && message.data.isNotEmpty()) {
            val title = message.data["title"] ?: "WeedX Alert"
            val body = message.data["message"] ?: message.data["body"] ?: ""
            
            showNotification(title, body, message.data)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            // Add data to intent for navigation
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Determine notification priority based on severity
        val priority = when (data["severity"]) {
            "critical" -> NotificationCompat.PRIORITY_HIGH
            "warning" -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }
        
        // Set notification icon based on alert type
        val icon = when (data["type"]) {
            "battery" -> R.drawable.ic_battery_alert
            "fault" -> R.drawable.ic_error
            "maintenance" -> R.drawable.ic_settings
            "detection" -> R.drawable.ic_eco
            else -> R.drawable.ic_notifications
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        
        // Add sound and vibration for high priority notifications
        if (priority == NotificationCompat.PRIORITY_HIGH) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        // Use timestamp or random ID to show multiple notifications
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "Notification shown: $title")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun sendTokenToBackend(token: String) {
        // TODO: Implement token registration with backend
        // This should be called through the repository/API service
        Log.d(TAG, "FCM token should be sent to backend: $token")
        
        // Store token in SharedPreferences for later upload
        getSharedPreferences("weedx_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .putBoolean("fcm_token_sent", false)
            .apply()
    }
}
