package com.example.weedx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weedx.data.models.response.WeedDetection
import com.example.weedx.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class WeedLogsAdapter(
    private val onItemClick: ((WeedDetection) -> Unit)? = null
) : ListAdapter<WeedDetection, WeedLogsAdapter.WeedLogViewHolder>(WeedDetectionDiffCallback()) {

    class WeedLogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weedIcon: ImageView = view.findViewById(R.id.weedIcon)
        val weedName: TextView = view.findViewById(R.id.weedName)
        val weedCount: TextView = view.findViewById(R.id.weedCount)
        val weedZone: TextView = view.findViewById(R.id.weedZone)
        val weedTime: TextView = view.findViewById(R.id.weedTime)
        val treatedBadge: CardView = view.findViewById(R.id.treatedBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeedLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weed_log, parent, false)
        return WeedLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeedLogViewHolder, position: Int) {
        val detection = getItem(position)
        
        holder.weedName.text = detection.weedType.replaceFirstChar { it.uppercase() }
        
        // Show confidence as percentage
        val confidencePercent = (detection.confidence * 100).toInt()
        holder.weedCount.text = "${confidencePercent}% confidence"
        
        // Show crop type or location if available
        val zoneText = detection.cropType?.let { "Crop: $it" } 
            ?: detection.location?.let { 
                if (it.latitude != null && it.longitude != null) {
                    "Lat: %.4f, Lng: %.4f".format(it.latitude, it.longitude)
                } else null
            } ?: "Unknown location"
        holder.weedZone.text = zoneText
        
        // Format time ago
        holder.weedTime.text = formatTimeAgo(detection.detectedAt)
        
        // Show treated badge if action was taken
        holder.treatedBadge.visibility = if (detection.action != null) View.VISIBLE else View.GONE
        
        // Load image if available
        detection.imageUrl?.let { url ->
            val fullUrl = Constants.getFullImageUrl(url)
            Glide.with(holder.itemView.context)
                .load(fullUrl)
                .placeholder(R.drawable.ic_leaf)
                .error(R.drawable.ic_leaf)
                .centerCrop()
                .into(holder.weedIcon)
        } ?: run {
            holder.weedIcon.setImageResource(R.drawable.ic_leaf)
        }
        
        // Click listener
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(detection)
        }
    }
    
    private fun formatTimeAgo(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString) ?: return dateString
            
            val now = System.currentTimeMillis()
            val diff = now - date.time
            
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            
            when {
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes min ago"
                else -> "Just now"
            }
        } catch (e: Exception) {
            dateString
        }
    }
    
    class WeedDetectionDiffCallback : DiffUtil.ItemCallback<WeedDetection>() {
        override fun areItemsTheSame(oldItem: WeedDetection, newItem: WeedDetection): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeedDetection, newItem: WeedDetection): Boolean {
            return oldItem == newItem
        }
    }
}
