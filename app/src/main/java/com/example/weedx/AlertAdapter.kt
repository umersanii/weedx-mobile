package com.example.weedx

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.data.models.response.Alert
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AlertAdapter(
    private val alerts: List<Alert>
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alertContainer: CardView = itemView.findViewById(R.id.alertContainer)
        val alertIcon: ImageView = itemView.findViewById(R.id.alertIcon)
        val alertType: TextView = itemView.findViewById(R.id.alertType)
        val alertMessage: TextView = itemView.findViewById(R.id.alertMessage)
        val alertTime: TextView = itemView.findViewById(R.id.alertTime)
        val severityIndicator: View = itemView.findViewById(R.id.severityIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        
        holder.alertType.text = alert.type
        holder.alertMessage.text = alert.message
        holder.alertTime.text = formatTimeAgo(alert.timestamp)
        
        // Set icon based on severity
        when (alert.severity.lowercase()) {
            "critical", "high" -> {
                holder.alertIcon.setImageResource(R.drawable.ic_alert_triangle)
                holder.severityIndicator.setBackgroundColor(Color.parseColor("#FF5252"))
                holder.alertIcon.setColorFilter(Color.parseColor("#FF5252"))
            }
            "medium", "warning" -> {
                holder.alertIcon.setImageResource(R.drawable.ic_alert_circle)
                holder.severityIndicator.setBackgroundColor(Color.parseColor("#FFC107"))
                holder.alertIcon.setColorFilter(Color.parseColor("#FFC107"))
            }
            else -> {
                holder.alertIcon.setImageResource(R.drawable.ic_info)
                holder.severityIndicator.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.alertIcon.setColorFilter(Color.parseColor("#4CAF50"))
            }
        }
        
        // Dim read alerts
        if (alert.isRead) {
            holder.alertContainer.alpha = 0.6f
        } else {
            holder.alertContainer.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = alerts.size
    
    private fun formatTimeAgo(timestamp: String): String {
        return try {
            // Try multiple date formats to handle various timestamp formats
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            )
            
            var date: Date? = null
            for (format in formats) {
                try {
                    date = format.parse(timestamp)
                    if (date != null) break
                } catch (e: Exception) {
                    // Try next format
                }
            }
            
            if (date == null) return timestamp
            
            val now = Date()
            val diff = now.time - date.time
            
            when {
                diff < 0 -> "Just now" // Handle future timestamps
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
                diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
                else -> {
                    // For older dates, show formatted date
                    val displayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                    displayFormat.format(date)
                }
            }
        } catch (e: Exception) {
            timestamp
        }
    }
}
