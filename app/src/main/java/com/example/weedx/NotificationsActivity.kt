package com.example.weedx

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weedx.adapters.AlertsAdapter
import com.example.weedx.presentation.viewmodels.AlertsViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val viewModel: AlertsViewModel by viewModels()
    private lateinit var alertsAdapter: AlertsAdapter

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var alertsRecyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var errorStateLayout: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_notifications)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupClickListeners()
        observeAlertsState()

        // Load alerts
        viewModel.loadAlerts(refresh = true)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        alertsRecyclerView = findViewById(R.id.alertsRecyclerView)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        errorStateLayout = findViewById(R.id.errorStateLayout)
        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)
    }

    private fun setupRecyclerView() {
        alertsAdapter = AlertsAdapter()
        alertsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = alertsAdapter
        }
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            finish()
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadAlerts(refresh = true)
        }

        retryButton.setOnClickListener {
            viewModel.loadAlerts(refresh = true)
        }
    }

    private fun observeAlertsState() {
        lifecycleScope.launch {
            viewModel.alertsState.collect { state ->
                when (state) {
                    is AlertsViewModel.AlertsState.Idle -> {
                        // Do nothing
                    }
                    is AlertsViewModel.AlertsState.Loading -> {
                        showLoading()
                    }
                    is AlertsViewModel.AlertsState.LoadingMore -> {
                        // Show loading indicator at bottom if needed
                        swipeRefreshLayout.isRefreshing = false
                    }
                    is AlertsViewModel.AlertsState.Success -> {
                        hideLoading()
                        swipeRefreshLayout.isRefreshing = false
                        
                        if (state.alerts.isEmpty()) {
                            showEmptyState()
                        } else {
                            showContent()
                            alertsAdapter.submitList(state.alerts)
                        }
                    }
                    is AlertsViewModel.AlertsState.Error -> {
                        hideLoading()
                        swipeRefreshLayout.isRefreshing = false
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        swipeRefreshLayout.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE
        errorStateLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingIndicator.visibility = View.GONE
    }

    private fun showContent() {
        swipeRefreshLayout.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        errorStateLayout.visibility = View.GONE
    }

    private fun showEmptyState() {
        swipeRefreshLayout.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        errorStateLayout.visibility = View.GONE
    }

    private fun showError(message: String) {
        swipeRefreshLayout.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE
        errorStateLayout.visibility = View.VISIBLE
        errorMessage.text = message
    }
}
