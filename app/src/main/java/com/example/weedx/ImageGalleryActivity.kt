package com.example.weedx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.presentation.viewmodels.GalleryUiState
import com.example.weedx.presentation.viewmodels.GalleryViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImageGalleryActivity : AppCompatActivity() {

    private val viewModel: GalleryViewModel by viewModels()
    
    private lateinit var imageGalleryRecyclerView: RecyclerView
    private lateinit var imageGalleryAdapter: ImageGalleryAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: View
    private lateinit var errorTextView: TextView
    private lateinit var retryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_image_gallery)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupGallery()
        setupBottomNavigation()
        observeUiState()
    }

    private fun initializeViews() {
        imageGalleryRecyclerView = findViewById(R.id.imageGalleryRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // Check if these views exist in the layout, otherwise skip
        progressBar = findViewById<ProgressBar?>(R.id.progressBar) ?: ProgressBar(this).apply {
            visibility = View.GONE
        }
        errorLayout = findViewById<View?>(R.id.errorLayout) ?: View(this).apply {
            visibility = View.GONE
        }
        errorTextView = findViewById<TextView?>(R.id.errorTextView) ?: TextView(this)
        retryButton = findViewById<Button?>(R.id.retryButton) ?: Button(this)
        
        retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun setupGallery() {
        // Setup RecyclerView with GridLayoutManager (2 columns)
        imageGalleryAdapter = ImageGalleryAdapter { image ->
            // Handle image click - could open full screen view
            // TODO: Implement full screen image viewer
        }
        
        imageGalleryRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ImageGalleryActivity, 2)
            adapter = imageGalleryAdapter
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is GalleryUiState.Loading -> {
                            showLoading()
                        }
                        is GalleryUiState.Success -> {
                            showSuccess(state.images)
                        }
                        is GalleryUiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        imageGalleryRecyclerView.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showSuccess(images: List<com.example.weedx.data.models.response.GalleryImage>) {
        progressBar.visibility = View.GONE
        imageGalleryRecyclerView.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        
        imageGalleryAdapter.submitList(images)
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        imageGalleryRecyclerView.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        errorTextView.text = message
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
                    val intent = Intent(this, WeatherActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_assistant -> {
                    val intent = Intent(this, AssistantActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_images -> {
                    // Already on images
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

        // Set images as selected
        bottomNavigation.selectedItemId = R.id.nav_images
    }
}
