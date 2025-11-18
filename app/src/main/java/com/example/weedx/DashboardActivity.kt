package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var liveCard: CardView
    private lateinit var weedsDetectedValue: TextView
    private lateinit var reportsButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)
        liveCard = findViewById(R.id.liveCard)
        weedsDetectedValue = findViewById(R.id.weedsDetectedValue)
        reportsButton = findViewById(R.id.reportsButton)

        // Set up click listeners
        setupClickListeners()
        
        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupClickListeners() {
        // Reports button click listener
        reportsButton.setOnClickListener {
            val intent = Intent(this, ReportsActivity::class.java)
            startActivity(intent)
        }
        
        // Live card click listener
        liveCard.setOnClickListener {
            val intent = Intent(this, LiveMonitoringActivity::class.java)
            startActivity(intent)
        }
        
        // Weeds detected card click listener - navigate to weed logs
        findViewById<CardView>(R.id.todaysSummaryCard).setOnClickListener {
            val intent = Intent(this, WeedLogsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_weather -> {
                    // Navigate to weather screen
                    val intent = Intent(this, WeatherActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_assistant -> {
                    // Navigate to assistant screen
                    val intent = Intent(this, AssistantActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_images -> {
                    // Navigate to image gallery screen
                    val intent = Intent(this, ImageGalleryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to profile screen
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Set home as selected
        bottomNavigation.selectedItemId = R.id.nav_home
    }
}
