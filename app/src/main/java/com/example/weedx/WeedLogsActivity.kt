package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.data.models.response.WeedDetection
import com.example.weedx.data.models.response.WeedSummary
import com.example.weedx.presentation.viewmodels.WeedLogsUiState
import com.example.weedx.presentation.viewmodels.WeedLogsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeedLogsActivity : AppCompatActivity() {

    private val viewModel: WeedLogsViewModel by viewModels()
    
    private lateinit var weedLogsRecyclerView: RecyclerView
    private lateinit var weedLogsAdapter: WeedLogsAdapter
    private lateinit var searchInput: EditText
    private lateinit var filterButton: CardView
    private lateinit var todayCount: TextView
    private lateinit var weekCount: TextView
    private lateinit var treatedPercent: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    
    // Loading and error states
    private var progressBar: ProgressBar? = null
    private var errorLayout: View? = null
    private var errorTextView: TextView? = null
    private var retryButton: Button? = null
    private var contentLayout: View? = null
    
    // Pie chart views (if we add them)
    private var chartContainer: View? = null
    private var chipGroup: ChipGroup? = null

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

        initializeViews()
        setupRecyclerView()
        setupSearch()
        setupBottomNavigation()
        observeUiState()
        
        // Load data
        viewModel.loadWeedLogs()
    }

    private fun initializeViews() {
        weedLogsRecyclerView = findViewById(R.id.weedLogsRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        filterButton = findViewById(R.id.filterButton)
        todayCount = findViewById(R.id.todayCount)
        weekCount = findViewById(R.id.weekCount)
        treatedPercent = findViewById(R.id.treatedPercent)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // Optional views - may not exist in current layout
        progressBar = findViewById(R.id.progressBar)
        errorLayout = findViewById(R.id.errorLayout)
        errorTextView = findViewById(R.id.errorTextView)
        retryButton = findViewById(R.id.retryButton)
        contentLayout = findViewById(R.id.contentLayout)
        chipGroup = findViewById(R.id.weedTypeChipGroup)
        
        retryButton?.setOnClickListener {
            viewModel.retry()
        }
        
        filterButton.setOnClickListener {
            // Toggle filter chips visibility
            chipGroup?.let {
                it.visibility = if (it.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setupRecyclerView() {
        weedLogsAdapter = WeedLogsAdapter { detection ->
            // Handle item click - could show detail dialog or navigate
            showDetectionDetail(detection)
        }
        
        weedLogsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WeedLogsActivity)
            adapter = weedLogsAdapter
        }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchDetections(s?.toString() ?: "")
            }
        })
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is WeedLogsUiState.Idle -> {
                                // Initial state, do nothing
                            }
                            is WeedLogsUiState.Loading -> {
                                showLoading()
                            }
                            is WeedLogsUiState.Success -> {
                                showSuccess(state)
                            }
                            is WeedLogsUiState.Error -> {
                                showError(state.message)
                            }
                        }
                    }
                }
                
                // Observe filtered detections separately
                launch {
                    viewModel.filteredDetections.collect { detections ->
                        weedLogsAdapter.submitList(detections)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        progressBar?.visibility = View.VISIBLE
        contentLayout?.visibility = View.GONE
        errorLayout?.visibility = View.GONE
        
        // If no dedicated progress bar, just disable interaction
        if (progressBar == null) {
            weedLogsRecyclerView.alpha = 0.5f
        }
    }

    private fun showSuccess(state: WeedLogsUiState.Success) {
        progressBar?.visibility = View.GONE
        contentLayout?.visibility = View.VISIBLE
        errorLayout?.visibility = View.GONE
        weedLogsRecyclerView.alpha = 1f
        
        // Update statistics cards
        todayCount.text = state.todayCount.toString()
        weekCount.text = formatCount(state.weekCount)
        treatedPercent.text = "${state.treatedPercent}%"
        
        // Setup filter chips based on summary
        setupFilterChips(state.summary)
    }

    private fun showError(message: String) {
        progressBar?.visibility = View.GONE
        contentLayout?.visibility = View.GONE
        errorLayout?.visibility = View.VISIBLE
        errorTextView?.text = message
        weedLogsRecyclerView.alpha = 1f
        
        // If no error layout, show as toast
        if (errorLayout == null) {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupFilterChips(summary: List<WeedSummary>) {
        chipGroup?.let { group ->
            group.removeAllViews()
            
            // Add "All" chip
            val allChip = Chip(this).apply {
                text = "All"
                isCheckable = true
                isChecked = true
                setOnClickListener {
                    viewModel.filterByWeedType(null)
                }
            }
            group.addView(allChip)
            
            // Add chip for each weed type
            summary.forEach { weedSummary ->
                val chip = Chip(this).apply {
                    text = "${weedSummary.weedType} (${weedSummary.count})"
                    isCheckable = true
                    setOnClickListener {
                        viewModel.filterByWeedType(weedSummary.weedType)
                    }
                }
                group.addView(chip)
            }
        }
    }
    
    private fun showDetectionDetail(detection: WeedDetection) {
        // Simple dialog showing detection details
        val message = buildString {
            appendLine("Type: ${detection.weedType}")
            appendLine("Confidence: ${(detection.confidence * 100).toInt()}%")
            detection.cropType?.let { appendLine("Crop: $it") }
            detection.location?.let { loc ->
                if (loc.latitude != null && loc.longitude != null) {
                    appendLine("Location: ${loc.latitude}, ${loc.longitude}")
                }
            }
            appendLine("Detected: ${detection.detectedAt}")
            detection.action?.let { appendLine("Action: $it") }
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(detection.weedType.replaceFirstChar { it.uppercase() })
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun formatCount(count: Int): String {
        return when {
            count >= 1000 -> String.format("%.1fK", count / 1000.0)
            else -> count.toString()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_weather -> {
                    startActivity(Intent(this, WeatherActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_assistant -> {
                    startActivity(Intent(this, AssistantActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_images -> {
                    startActivity(Intent(this, ImageGalleryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
