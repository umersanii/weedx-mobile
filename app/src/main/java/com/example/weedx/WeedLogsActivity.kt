package com.example.weedx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WeedLogsActivity : AppCompatActivity() {

    private lateinit var weedLogsRecyclerView: RecyclerView
    private lateinit var weedLogsAdapter: WeedLogsAdapter
    private val weedLogs = mutableListOf<WeedLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_weed_logs)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        weedLogsRecyclerView = findViewById(R.id.weedLogsRecyclerView)

        setupWeedLogs()
    }

    private fun setupWeedLogs() {
        // Create sample weed logs
        weedLogs.apply {
            add(WeedLog(
                name = "Dandelion",
                weedsCount = 23,
                zone = "A-12",
                timeAgo = "2 min ago",
                isTreated = true
            ))
            add(WeedLog(
                name = "Crabgrass",
                weedsCount = 15,
                zone = "A-12",
                timeAgo = "8 min ago",
                isTreated = true
            ))
            add(WeedLog(
                name = "Thistle",
                weedsCount = 8,
                zone = "A-11",
                timeAgo = "15 min ago",
                isTreated = true
            ))
            add(WeedLog(
                name = "Dandelion",
                weedsCount = 31,
                zone = "A-11",
                timeAgo = "22 min ago",
                isTreated = true
            ))
            add(WeedLog(
                name = "Pigweed",
                weedsCount = 19,
                zone = "A-11",
                timeAgo = "28 min ago",
                isTreated = true
            ))
            add(WeedLog(
                name = "Crabgrass",
                weedsCount = 12,
                zone = "A-10",
                timeAgo = "35 min ago",
                isTreated = true
            ))
            add(WeedLog(
                name = "Thistle",
                weedsCount = 26,
                zone = "A-10",
                timeAgo = "42 min ago",
                isTreated = true
            ))
            add(WeedLog(
                name = "Dandelion",
                weedsCount = 18,
                zone = "A-09",
                timeAgo = "1 hour ago",
                isTreated = true
            ))
        }

        // Setup RecyclerView
        weedLogsAdapter = WeedLogsAdapter(weedLogs)
        weedLogsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WeedLogsActivity)
            adapter = weedLogsAdapter
        }
    }
}
