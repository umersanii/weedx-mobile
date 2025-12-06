package com.example.weedx

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weedx.presentation.viewmodels.MonitoringViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LiveMonitoringActivity : AppCompatActivity() {

    private val viewModel: MonitoringViewModel by viewModels()
    
    private lateinit var timelineRecyclerView: RecyclerView
    private lateinit var timelineAdapter: TimelineAdapter
    private lateinit var batteryValue: TextView
    private lateinit var herbicideValue: TextView
    private lateinit var coverageValue: TextView
    private lateinit var efficiencyValue: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var lastUpdatedText: TextView
    private val timelineEvents = mutableListOf<TimelineEvent>()
    
    // Auto-refresh handler
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 60000L // 60 seconds (1 minute)
    private val refreshRunnable = object : Runnable {
        override fun run() {
            viewModel.refresh()
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_live_monitoring)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        timelineRecyclerView = findViewById(R.id.timelineRecyclerView)
        batteryValue = findViewById(R.id.batteryValue)
        herbicideValue = findViewById(R.id.herbicideValue)
        coverageValue = findViewById(R.id.coverageValue)
        efficiencyValue = findViewById(R.id.efficiencyValue)
        lastUpdatedText = findViewById(R.id.lastUpdatedText)

        setupTimeline()
        observeViewModel()
        startAutoRefresh()
    }
    
    override fun onResume() {
        super.onResume()
        // Resume auto-refresh when activity is visible
        startAutoRefresh()
    }
    
    override fun onPause() {
        super.onPause()
        // Stop auto-refresh when activity is not visible
        stopAutoRefresh()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up handler callbacks
        stopAutoRefresh()
    }
    
    private fun startAutoRefresh() {
        // Start periodic refresh
        refreshHandler.removeCallbacks(refreshRunnable)
        refreshHandler.post(refreshRunnable)
    }
    
    private fun stopAutoRefresh() {
        // Stop periodic refresh
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun setupTimeline() {
        // Create sample timeline events
        timelineEvents.apply {
            add(TimelineEvent(
                title = "Weed cluster detected",
                description = "17 weeds treated",
                timeAgo = "2 minutes ago"
            ))
            add(TimelineEvent(
                title = "Zone transition",
                description = "Moving from A-11 to A-12",
                timeAgo = "8 minutes ago"
            ))
            add(TimelineEvent(
                title = "High density area",
                description = "47 weeds detected in 50m²",
                timeAgo = "15 minutes ago"
            ))
            add(TimelineEvent(
                title = "Session started",
                description = "Field A operation begin",
                timeAgo = "4 hours ago",
                isLast = true
            ))
        }

        // Setup RecyclerView
        timelineAdapter = TimelineAdapter(timelineEvents)
        timelineRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LiveMonitoringActivity)
            adapter = timelineAdapter
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.monitoringState.collect { state ->
                when (state) {
                    is MonitoringViewModel.MonitoringState.Idle -> {
                        // Initial state
                    }
                    is MonitoringViewModel.MonitoringState.Loading -> {
                        // Show loading state (optional - could show a small indicator)
                    }
                    is MonitoringViewModel.MonitoringState.Success -> {
                        updateUI(state.data)
                        updateLastRefreshTime()
                    }
                    is MonitoringViewModel.MonitoringState.Error -> {
                        Toast.makeText(
                            this@LiveMonitoringActivity,
                            "Error: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    
    private fun updateLastRefreshTime() {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        lastUpdatedText.text = "Last updated: $currentTime"
    }
    
    private fun updateUI(data: com.example.weedx.data.models.response.MonitoringResponse) {
        // Update metrics
        batteryValue.text = "${data.metrics.battery}%"
        herbicideValue.text = String.format("%.1fL", data.metrics.herbicideLevel)
        coverageValue.text = String.format("%.1fha", data.metrics.coverage)
        efficiencyValue.text = String.format("%.0f%%", data.metrics.efficiency)
        
        // Update timeline with real data
        timelineEvents.clear()
        data.activityTimeline?.forEachIndexed { index, activity ->
            timelineEvents.add(
                TimelineEvent(
                    title = activity.action,
                    description = activity.description ?: "",
                    timeAgo = formatTimeAgo(activity.timestamp),
                    isLast = index == data.activityTimeline.size - 1
                )
            )
        }
        timelineAdapter.notifyDataSetChanged()
    }
    
    private fun formatTimeAgo(timestamp: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(timestamp) ?: return timestamp
            val now = Date()
            val diff = now.time - date.time
            
            when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes ago"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
                else -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
            }
        } catch (e: Exception) {
            timestamp
        }
    }
}
