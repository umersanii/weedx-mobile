package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.weedx.data.models.response.GalleryImage

class ImageGalleryAdapter(
    private val onImageClick: ((GalleryImage) -> Unit)? = null
) : ListAdapter<GalleryImage, ImageGalleryAdapter.ImageViewHolder>(GalleryDiffCallback()) {

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
        val image = getItem(position)
        
        // Display weed type as label
        holder.zoneLabel.text = image.weedType
        
        // Load image using Coil
        if (image.url.isNotEmpty()) {
            holder.placeholderIcon.visibility = View.GONE
            holder.galleryImage.visibility = View.VISIBLE
            
            holder.galleryImage.load(image.url) {
                crossfade(true)
                placeholder(R.drawable.ic_info) // Use as placeholder
                error(R.drawable.ic_info) // Use as error placeholder
                transformations(RoundedCornersTransformation(16f))
            }
        } else {
            // Show placeholder if no URL
            holder.placeholderIcon.visibility = View.VISIBLE
            holder.galleryImage.visibility = View.GONE
        }
        
        // Handle click
        holder.itemView.setOnClickListener {
            onImageClick?.invoke(image)
        }
    }

    class GalleryDiffCallback : DiffUtil.ItemCallback<GalleryImage>() {
        override fun areItemsTheSame(oldItem: GalleryImage, newItem: GalleryImage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GalleryImage, newItem: GalleryImage): Boolean {
            return oldItem == newItem
        }
    }
}
