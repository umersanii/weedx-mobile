package com.example.weedx

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ImageGalleryActivity : AppCompatActivity() {

    private lateinit var imageGalleryRecyclerView: RecyclerView
    private lateinit var imageGalleryAdapter: ImageGalleryAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private val galleryImages = mutableListOf<GalleryImage>()

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

        // Initialize views
        imageGalleryRecyclerView = findViewById(R.id.imageGalleryRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupGallery()
        setupBottomNavigation()
    }

    private fun setupGallery() {
        // Create sample gallery images (all from zone A-12 as shown in design)
        galleryImages.apply {
            repeat(8) {
                add(GalleryImage(zone = "A-12"))
            }
        }

        // Setup RecyclerView with GridLayoutManager (2 columns)
        imageGalleryAdapter = ImageGalleryAdapter(galleryImages)
        imageGalleryRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ImageGalleryActivity, 2)
            adapter = imageGalleryAdapter
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
