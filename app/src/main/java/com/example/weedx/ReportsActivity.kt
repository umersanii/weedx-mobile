package com.example.weedx

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReportsActivity : AppCompatActivity() {

    private lateinit var weeklyTab: TextView
    private lateinit var monthlyTab: TextView
    private lateinit var trendChartRecyclerView: RecyclerView
    private lateinit var distributionRecyclerView: RecyclerView
    private lateinit var downloadButton: CardView

    private var isWeeklySelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_reports)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupTabs()
        setupCharts()
        setupDownloadButton()
    }

    private fun initViews() {
        weeklyTab = findViewById(R.id.weeklyTab)
        monthlyTab = findViewById(R.id.monthlyTab)
        trendChartRecyclerView = findViewById(R.id.trendChartRecyclerView)
        distributionRecyclerView = findViewById(R.id.distributionRecyclerView)
        downloadButton = findViewById(R.id.downloadButton)
    }

    private fun setupTabs() {
        weeklyTab.setOnClickListener {
            if (!isWeeklySelected) {
                selectWeeklyTab()
                updateChartsForWeekly()
            }
        }

        monthlyTab.setOnClickListener {
            if (isWeeklySelected) {
                selectMonthlyTab()
                updateChartsForMonthly()
            }
        }
    }

    private fun selectWeeklyTab() {
        isWeeklySelected = true
        weeklyTab.setBackgroundResource(R.drawable.tab_selected_green)
        weeklyTab.setTextColor(Color.WHITE)
        monthlyTab.background = null
        monthlyTab.setTextColor(Color.BLACK)
    }

    private fun selectMonthlyTab() {
        isWeeklySelected = false
        monthlyTab.setBackgroundResource(R.drawable.tab_selected_green)
        monthlyTab.setTextColor(Color.WHITE)
        weeklyTab.background = null
        weeklyTab.setTextColor(Color.BLACK)
    }

    private fun setupCharts() {
        // Setup Trend Chart
        trendChartRecyclerView.layoutManager = LinearLayoutManager(this)
        updateChartsForWeekly()

        // Setup Distribution Chart
        setupDistributionChart()
    }

    private fun updateChartsForWeekly() {
        val weeklyData = listOf(
            TrendData("Mon", 156),
            TrendData("Tue", 189),
            TrendData("Wed", 145),
            TrendData("Thu", 198),
            TrendData("Fri", 223),
            TrendData("Sat", 187),
            TrendData("Sun", 149)
        )
        trendChartRecyclerView.adapter = TrendChartAdapter(weeklyData)
    }

    private fun updateChartsForMonthly() {
        val monthlyData = listOf(
            TrendData("Week 1", 890),
            TrendData("Week 2", 1120),
            TrendData("Week 3", 945),
            TrendData("Week 4", 1350)
        )
        trendChartRecyclerView.adapter = TrendChartAdapter(monthlyData)
    }

    private fun setupDistributionChart() {
        val distributions = listOf(
            WeedDistribution("Broadleaf Weed", 38, ContextCompat.getColor(this, R.color.green_primary)),
            WeedDistribution("Grassy Weed", 29, ContextCompat.getColor(this, R.color.green_light)),
            WeedDistribution("Sedge Weed", 18, Color.parseColor("#81C784")),
            WeedDistribution("Woody Weed", 10, Color.parseColor("#A5D6A7")),
            WeedDistribution("Others", 5, Color.parseColor("#C8E6C9"))
        )
        
        distributionRecyclerView.layoutManager = LinearLayoutManager(this)
        distributionRecyclerView.adapter = WeedDistributionAdapter(distributions)
    }

    private fun setupDownloadButton() {
        downloadButton.setOnClickListener {
            Toast.makeText(this, "Downloading report...", Toast.LENGTH_SHORT).show()
        }
    }
}
