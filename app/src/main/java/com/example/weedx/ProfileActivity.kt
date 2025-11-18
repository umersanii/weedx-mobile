package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var notificationsItem: LinearLayout
    private lateinit var appSettingsItem: LinearLayout
    private lateinit var helpSupportItem: LinearLayout
    private lateinit var logoutButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        notificationsItem = findViewById(R.id.notificationsItem)
        appSettingsItem = findViewById(R.id.appSettingsItem)
        helpSupportItem = findViewById(R.id.helpSupportItem)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun setupClickListeners() {
        notificationsItem.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Notifications screen
        }

        appSettingsItem.setOnClickListener {
            Toast.makeText(this, "App Settings", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to App Settings screen
        }

        helpSupportItem.setOnClickListener {
            Toast.makeText(this, "Help & Support", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Help & Support screen
        }

        logoutButton.setOnClickListener {
            // Navigate back to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_weather -> {
                    val intent = Intent(this, WeatherActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_assistant -> {
                    val intent = Intent(this, AssistantActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_images -> {
                    val intent = Intent(this, ImageGalleryActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile screen
                    true
                }
                else -> false
            }
        }

        // Set profile as selected
        bottomNavigation.selectedItemId = R.id.nav_profile
    }
}
