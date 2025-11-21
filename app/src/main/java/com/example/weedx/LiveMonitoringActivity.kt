package com.example.weedx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LiveMonitoringActivity : AppCompatActivity() {

    private lateinit var timelineRecyclerView: RecyclerView
    private lateinit var timelineAdapter: TimelineAdapter
    private val timelineEvents = mutableListOf<TimelineEvent>()

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

        setupTimeline()
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
}
