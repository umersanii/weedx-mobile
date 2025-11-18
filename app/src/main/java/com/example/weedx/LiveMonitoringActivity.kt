package com.example.weedx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LiveMonitoringActivity : AppCompatActivity() {

    private lateinit var timelineRecyclerView: RecyclerView
    private lateinit var timelineAdapter: TimelineAdapter
    private val timelineEvents = mutableListOf<TimelineEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_monitoring)

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
