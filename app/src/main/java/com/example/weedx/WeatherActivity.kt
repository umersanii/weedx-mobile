package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.data.models.response.SoilData
import com.example.weedx.data.models.response.WeatherForecast
import com.example.weedx.presentation.viewmodels.EnvironmentViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    private val viewModel: EnvironmentViewModel by viewModels()

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var soilRecyclerView: RecyclerView
    
    // Current weather views
    private lateinit var currentTemperature: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var windSpeed: TextView
    private lateinit var uvIndex: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_weather)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupBottomNavigation()
        observeViewModel()
        
        // Load data from API
        viewModel.loadAllEnvironmentData()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView)
        soilRecyclerView = findViewById(R.id.soilRecyclerView)
        
        // Current weather views
        currentTemperature = findViewById(R.id.currentTemperature)
        weatherDescription = findViewById(R.id.weatherDescription)
        windSpeed = findViewById(R.id.windSpeed)
        uvIndex = findViewById(R.id.uvIndex)
        
        // Set up RecyclerViews
        forecastRecyclerView.layoutManager = LinearLayoutManager(this)
        soilRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Set default values
        resetToDefaults()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is EnvironmentViewModel.WeatherState.Idle -> { }
                    is EnvironmentViewModel.WeatherState.Loading -> { }
                    is EnvironmentViewModel.WeatherState.Success -> {
                        updateCurrentWeatherUI(state.weather)
                    }
                    is EnvironmentViewModel.WeatherState.Error -> {
                        Toast.makeText(this@WeatherActivity, "Weather error: ${state.message}", Toast.LENGTH_SHORT).show()
                        resetWeatherToDefaults()
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.forecastState.collect { state ->
                when (state) {
                    is EnvironmentViewModel.ForecastState.Idle -> { }
                    is EnvironmentViewModel.ForecastState.Loading -> { }
                    is EnvironmentViewModel.ForecastState.Success -> {
                        updateForecastUI(state.forecast)
                    }
                    is EnvironmentViewModel.ForecastState.Error -> {
                        setupDefaultForecast()
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.soilState.collect { state ->
                when (state) {
                    is EnvironmentViewModel.SoilState.Idle -> { }
                    is EnvironmentViewModel.SoilState.Loading -> { }
                    is EnvironmentViewModel.SoilState.Success -> {
                        updateSoilUI(state.soil)
                    }
                    is EnvironmentViewModel.SoilState.Error -> {
                        setupDefaultSoilMetrics()
                    }
                }
            }
        }
    }
    
    private fun updateCurrentWeatherUI(weather: com.example.weedx.data.models.response.CurrentWeather) {
        currentTemperature.text = String.format("%.0f°C", weather.temperature)
        weatherDescription.text = weather.weatherCondition.ifEmpty { "--" }
        windSpeed.text = weather.windSpeed?.let { String.format("%.0f km/h", it) } ?: "--"
        uvIndex.text = weather.uvIndex?.toString() ?: "--"
    }
    
    private fun updateForecastUI(forecast: List<WeatherForecast>) {
        if (forecast.isEmpty()) {
            setupDefaultForecast()
            return
        }
        
        val forecastDays = forecast.map { day ->
            val iconRes = getWeatherIcon(day.weatherCondition)
            ForecastDay(
                day = formatDayOfWeek(day.date),
                description = day.weatherCondition,
                icon = iconRes,
                maxTemp = day.tempHigh.toInt(),
                minTemp = day.tempLow.toInt()
            )
        }
        forecastRecyclerView.adapter = ForecastAdapter(forecastDays)
    }
    
    private fun updateSoilUI(soil: SoilData) {
        val metrics = listOf(
            SoilMetric("Moisture", String.format("%.0f%%", soil.moisture), R.drawable.ic_droplet, getMoistureStatus(soil.moisture)),
            SoilMetric("Temperature", String.format("%.1f°C", soil.temperature), R.drawable.ic_temperature),
            SoilMetric("pH Level", String.format("%.1f", soil.ph), R.drawable.ic_ph),
            SoilMetric("Nitrogen", soil.nitrogen?.let { getNutrientLevel(it) } ?: "--", R.drawable.ic_nitrogen),
            SoilMetric("Phosphorus", soil.phosphorus?.let { getNutrientLevel(it) } ?: "--", R.drawable.ic_phosphorus),
            SoilMetric("Potassium", soil.potassium?.let { getNutrientLevel(it) } ?: "--", R.drawable.ic_potassium)
        )
        soilRecyclerView.adapter = SoilMetricAdapter(metrics)
    }
    
    private fun resetToDefaults() {
        resetWeatherToDefaults()
        setupDefaultForecast()
        setupDefaultSoilMetrics()
    }
    
    private fun resetWeatherToDefaults() {
        currentTemperature.text = "--°C"
        weatherDescription.text = "--"
        windSpeed.text = "--"
        uvIndex.text = "--"
    }

    private fun setupDefaultForecast() {
        val forecasts = listOf(
            ForecastDay("--", "--", R.drawable.ic_sunny, 0, 0),
            ForecastDay("--", "--", R.drawable.ic_cloudy, 0, 0),
            ForecastDay("--", "--", R.drawable.ic_sunny, 0, 0)
        )
        forecastRecyclerView.adapter = ForecastAdapter(forecasts)
    }

    private fun setupDefaultSoilMetrics() {
        val metrics = listOf(
            SoilMetric("Moisture", "--", R.drawable.ic_droplet, "--"),
            SoilMetric("Temperature", "--", R.drawable.ic_temperature),
            SoilMetric("pH Level", "--", R.drawable.ic_ph),
            SoilMetric("Nitrogen", "--", R.drawable.ic_nitrogen),
            SoilMetric("Phosphorus", "--", R.drawable.ic_phosphorus),
            SoilMetric("Potassium", "--", R.drawable.ic_potassium)
        )
        soilRecyclerView.adapter = SoilMetricAdapter(metrics)
    }
    
    private fun getWeatherIcon(condition: String): Int {
        return when (condition.lowercase()) {
            "sunny", "clear" -> R.drawable.ic_sunny
            "partly cloudy" -> R.drawable.ic_partly_cloudy
            "cloudy", "overcast" -> R.drawable.ic_cloudy
            "rain", "rainy", "showers" -> R.drawable.ic_cloudy
            else -> R.drawable.ic_partly_cloudy
        }
    }
    
    private fun formatDayOfWeek(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun getMoistureStatus(moisture: Double): String {
        return when {
            moisture < 30 -> "Low"
            moisture < 60 -> "Good"
            else -> "High"
        }
    }
    
    private fun getNutrientLevel(value: Double): String {
        return when {
            value < 30 -> "Low"
            value < 60 -> "Medium"
            else -> "High"
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
