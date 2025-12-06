package com.example.weedx

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import coil.load
import com.example.weedx.data.models.response.GalleryImage
import com.example.weedx.utils.Constants
import com.github.chrisbanes.photoview.PhotoView
import java.text.SimpleDateFormat
import java.util.Locale

class ImageFullscreenDialog(
    context: Context,
    private val image: GalleryImage
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    private lateinit var fullscreenImage: PhotoView
    private lateinit var closeButton: ImageView
    private lateinit var weedTypeText: TextView
    private lateinit var confidenceText: TextView
    private lateinit var locationText: TextView
    private lateinit var capturedDateText: TextView
    private lateinit var confidenceLayout: LinearLayout
    private lateinit var locationLayout: LinearLayout
    private lateinit var dateLayout: LinearLayout
    private lateinit var loadingProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_image_fullscreen)
        
        // Make dialog fullscreen
        window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        initializeViews()
        setupImage()
        setupDetails()
    }

    private fun initializeViews() {
        fullscreenImage = findViewById(R.id.fullscreenImage)
        closeButton = findViewById(R.id.closeButton)
        weedTypeText = findViewById(R.id.weedTypeText)
        confidenceText = findViewById(R.id.confidenceText)
        locationText = findViewById(R.id.locationText)
        capturedDateText = findViewById(R.id.capturedDateText)
        confidenceLayout = findViewById(R.id.confidenceLayout)
        locationLayout = findViewById(R.id.locationLayout)
        dateLayout = findViewById(R.id.dateLayout)
        loadingProgress = findViewById(R.id.loadingProgress)

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setupImage() {
        val imageUrl = Constants.getFullImageUrl(image.url)
        
        if (!imageUrl.isNullOrEmpty()) {
            loadingProgress.visibility = View.VISIBLE
            
            fullscreenImage.load(imageUrl) {
                crossfade(true)
                error(R.drawable.ic_info)
                listener(
                    onSuccess = { _, _ ->
                        loadingProgress.visibility = View.GONE
                    },
                    onError = { _, _ ->
                        loadingProgress.visibility = View.GONE
                    }
                )
            }
        } else {
            loadingProgress.visibility = View.GONE
        }
    }

    private fun setupDetails() {
        // Weed type
        weedTypeText.text = image.weedType

        // Confidence
        if (image.confidence != null && image.confidence > 0) {
            confidenceLayout.visibility = View.VISIBLE
            confidenceText.text = String.format(Locale.US, "%.1f%%", image.confidence)
        }

        // Location
        if (image.location != null) {
            locationLayout.visibility = View.VISIBLE
            locationText.text = String.format(
                Locale.US,
                "%.6f, %.6f",
                image.location.latitude,
                image.location.longitude
            )
        }

        // Captured date
        if (!image.capturedAt.isNullOrEmpty()) {
            dateLayout.visibility = View.VISIBLE
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
                val date = inputFormat.parse(image.capturedAt)
                capturedDateText.text = date?.let { outputFormat.format(it) } ?: image.capturedAt
            } catch (e: Exception) {
                capturedDateText.text = image.capturedAt
            }
        }
    }
}
