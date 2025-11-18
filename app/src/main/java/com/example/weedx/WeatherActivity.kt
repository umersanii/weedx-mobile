package com.example.weedx

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class WeatherActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var soilRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        initViews()
        setupForecast()
        setupSoilMetrics()
        setupBottomNavigation()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView)
        soilRecyclerView = findViewById(R.id.soilRecyclerView)
    }

    private fun setupForecast() {
        val forecasts = listOf(
            ForecastDay("Mon", "Sunny", R.drawable.ic_sunny, 26, 18),
            ForecastDay("Tue", "Cloudy", R.drawable.ic_cloudy, 25, 17),
            ForecastDay("Wed", "Sunny", R.drawable.ic_sunny, 24, 16),
            ForecastDay("Thu", "Cloudy", R.drawable.ic_cloudy, 23, 15),
            ForecastDay("Fri", "Sunny", R.drawable.ic_sunny, 22, 14),
            ForecastDay("Sat", "Cloudy", R.drawable.ic_cloudy, 21, 13),
            ForecastDay("Sun", "Sunny", R.drawable.ic_sunny, 20, 12)
        )

        forecastRecyclerView.layoutManager = LinearLayoutManager(this)
        forecastRecyclerView.adapter = ForecastAdapter(forecasts)
    }

    private fun setupSoilMetrics() {
        val metrics = listOf(
            SoilMetric("Moisture", "45%", R.drawable.ic_droplet, "Good"),
            SoilMetric("Temperature", "22°C", R.drawable.ic_temperature),
            SoilMetric("pH Level", "6.5", R.drawable.ic_ph),
            SoilMetric("Nitrogen", "High", R.drawable.ic_nitrogen),
            SoilMetric("Phosphorus", "Medium", R.drawable.ic_phosphorus),
            SoilMetric("Potassium", "High", R.drawable.ic_potassium)
        )

        soilRecyclerView.layoutManager = LinearLayoutManager(this)
        soilRecyclerView.adapter = SoilMetricAdapter(metrics)
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
                    // Already on weather screen
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
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Set weather as selected
        bottomNavigation.selectedItemId = R.id.nav_weather
    }
}
