package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.presentation.viewmodels.DashboardViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var liveCard: CardView
    private lateinit var weatherQuickCard: CardView
    private lateinit var weedLogsQuickCard: CardView
    private lateinit var weedsDetectedValue: TextView
    private lateinit var herbicideUsedValue: TextView
    private lateinit var areaCoveredValue: TextView
    private lateinit var batteryValue: TextView
    private lateinit var locationValue: TextView
    private lateinit var speedValue: TextView
    private lateinit var reportsButton: CardView
    private lateinit var alertsRecyclerView: RecyclerView
    private lateinit var noAlertsText: TextView
    private lateinit var alertAdapter: AlertAdapter
    
    // Quick card values
    private lateinit var weatherTempValue: TextView
    private lateinit var totalWeedsValue: TextView
    private lateinit var robotStatusQuickValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_dashboard)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)
        liveCard = findViewById(R.id.liveCard)
        weatherQuickCard = findViewById(R.id.weatherQuickCard)
        weedLogsQuickCard = findViewById(R.id.weedLogsQuickCard)
        weedsDetectedValue = findViewById(R.id.weedsDetectedValue)
        herbicideUsedValue = findViewById(R.id.herbicideUsedValue)
        areaCoveredValue = findViewById(R.id.areaCoveredValue)
        batteryValue = findViewById(R.id.batteryValue)
        locationValue = findViewById(R.id.locationValue)
        speedValue = findViewById(R.id.speedValue)
        reportsButton = findViewById(R.id.reportsButton)
        
        // Quick card values
        weatherTempValue = findViewById(R.id.weatherTempValue)
        totalWeedsValue = findViewById(R.id.totalWeedsValue)
        robotStatusQuickValue = findViewById(R.id.robotStatusQuickValue)

        // Setup alerts RecyclerView
        setupAlertsRecyclerView()

        // Set up click listeners
        setupClickListeners()
        
        // Set up bottom navigation
        setupBottomNavigation()
        
        // Observe ViewModels
        observeViewModel()
        
        // Load data after UI is set up
        viewModel.loadDashboardData()
        viewModel.loadAlerts()
        viewModel.loadWeather()
    }
    
    private fun setupAlertsRecyclerView() {
        try {
            alertsRecyclerView = findViewById<RecyclerView>(R.id.alertsRecyclerView).apply {
                layoutManager = LinearLayoutManager(this@DashboardActivity)
                visibility = View.GONE
            }
            noAlertsText = findViewById(R.id.noAlertsText)
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up alerts: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            try {
                viewModel.dashboardState.collect { state ->
                    when (state) {
                        is DashboardViewModel.DashboardState.Idle -> {
                            // Initial state - show defaults
                        }
                        is DashboardViewModel.DashboardState.Loading -> {
                            // Show loading state (could add shimmer here)
                        }
                        is DashboardViewModel.DashboardState.Success -> {
                            updateDashboardUI(state.data)
                        }
                        is DashboardViewModel.DashboardState.Error -> {
                            Toast.makeText(
                                this@DashboardActivity,
                                "Error: ${state.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetUIToDefaults()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DashboardActivity,
                    "Dashboard error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                resetUIToDefaults()
            }
        }
        
        lifecycleScope.launch {
            try {
                viewModel.alertsState.collect { state ->
                    when (state) {
                        is DashboardViewModel.AlertsState.Idle -> {
                            // Initial state
                        }
                        is DashboardViewModel.AlertsState.Loading -> {
                            // Show loading state
                        }
                        is DashboardViewModel.AlertsState.Success -> {
                            updateAlertsUI(state.alerts)
                        }
                        is DashboardViewModel.AlertsState.Error -> {
                            noAlertsText.text = "Failed to load alerts"
                            noAlertsText.visibility = View.VISIBLE
                            alertsRecyclerView.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DashboardActivity,
                    "Alerts error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        lifecycleScope.launch {
            try {
                viewModel.weatherState.collect { state ->
                    when (state) {
                        is DashboardViewModel.WeatherState.Idle -> {
                            // Initial state
                        }
                        is DashboardViewModel.WeatherState.Loading -> {
                            // Show loading state
                        }
                        is DashboardViewModel.WeatherState.Success -> {
                            updateWeatherUI(state.weather)
                        }
                        is DashboardViewModel.WeatherState.Error -> {
                            weatherTempValue.text = "--°C"
                        }
                    }
                }
            } catch (e: Exception) {
                weatherTempValue.text = "--°C"
            }
        }
    }
    
    private fun updateDashboardUI(data: com.example.weedx.data.models.response.DashboardResponse) {
        try {
            // Update robot status
            batteryValue.text = if (data.robotStatus.battery >= 0) "${data.robotStatus.battery}%" else "--"
            locationValue.text = data.robotStatus.status.ifEmpty { "--" }
            speedValue.text = data.robotStatus.speed?.let { String.format("%.1f", it) } ?: "--"
            
            // Update quick card for robot status
            robotStatusQuickValue.text = data.robotStatus.status.ifEmpty { "--" }
            
            // Update today's summary
            weedsDetectedValue.text = if (data.todaySummary.weedsDetected >= 0) data.todaySummary.weedsDetected.toString() else "--"
            totalWeedsValue.text = if (data.todaySummary.weedsDetected >= 0) data.todaySummary.weedsDetected.toString() else "--"
            herbicideUsedValue.text = if (data.todaySummary.herbicideUsed >= 0) String.format("%.1fL", data.todaySummary.herbicideUsed) else "--"
            areaCoveredValue.text = if (data.todaySummary.areaCovered >= 0) String.format("%.1fha", data.todaySummary.areaCovered) else "--"
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating UI: ${e.message}", Toast.LENGTH_SHORT).show()
            resetUIToDefaults()
        }
    }
    
    private fun resetUIToDefaults() {
        batteryValue.text = "--"
        locationValue.text = "--"
        speedValue.text = "--"
        weedsDetectedValue.text = "--"
        herbicideUsedValue.text = "--"
        areaCoveredValue.text = "--"
        totalWeedsValue.text = "--"
        robotStatusQuickValue.text = "--"
        weatherTempValue.text = "--°C"
    }
    
    private fun updateWeatherUI(weather: com.example.weedx.data.models.response.CurrentWeather) {
        try {
            weatherTempValue.text = String.format("%.0f°C", weather.temperature)
        } catch (e: Exception) {
            weatherTempValue.text = "--°C"
        }
    }
    
    private fun updateAlertsUI(alerts: List<com.example.weedx.data.models.response.Alert>) {
        try {
            if (alerts.isEmpty()) {
                noAlertsText.visibility = View.VISIBLE
                alertsRecyclerView.visibility = View.GONE
                noAlertsText.text = "No recent alerts"
            } else {
                noAlertsText.visibility = View.GONE
                alertsRecyclerView.visibility = View.VISIBLE
                alertAdapter = AlertAdapter(alerts)
                alertsRecyclerView.adapter = alertAdapter
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying alerts: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
        
        // Weather quick card click listener
        weatherQuickCard.setOnClickListener {
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
        }
        
        // Weed logs quick card click listener
        weedLogsQuickCard.setOnClickListener {
            val intent = Intent(this, WeedLogsActivity::class.java)
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
