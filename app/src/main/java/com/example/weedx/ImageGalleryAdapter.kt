package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ImageGalleryAdapter(private val images: List<GalleryImage>) :
    RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val galleryImage: ImageView = view.findViewById(R.id.galleryImage)
        val placeholderIcon: ImageView = view.findViewById(R.id.placeholderIcon)
        val zoneLabel: TextView = view.findViewById(R.id.zoneLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_gallery, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        
        holder.zoneLabel.text = image.zone
        
        // For now, show placeholder icon (in a real app, load actual images)
        holder.placeholderIcon.visibility = View.VISIBLE
        holder.galleryImage.visibility = View.GONE
        
        // If you have actual images, you would load them like this:
        // if (image.imagePath != null) {
        //     holder.placeholderIcon.visibility = View.GONE
        //     holder.galleryImage.visibility = View.VISIBLE
        //     // Load image using Glide/Picasso/Coil
        // }
    }

    override fun getItemCount(): Int = images.size
}
