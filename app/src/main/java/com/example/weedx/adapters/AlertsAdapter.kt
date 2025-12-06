package com.example.weedx.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.R
import com.example.weedx.data.models.response.Alert
import java.text.SimpleDateFormat
import java.util.*

class AlertsAdapter : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    private val alerts = mutableListOf<Alert>()

    fun submitList(newAlerts: List<Alert>) {
        alerts.clear()
        alerts.addAll(newAlerts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert_notification, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount(): Int = alerts.size

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val alertIconCard: CardView = itemView.findViewById(R.id.alertIconCard)
        private val alertIcon: ImageView = itemView.findViewById(R.id.alertIcon)
        private val alertType: TextView = itemView.findViewById(R.id.alertType)
        private val alertTime: TextView = itemView.findViewById(R.id.alertTime)
        private val alertMessage: TextView = itemView.findViewById(R.id.alertMessage)
        private val severityBadge: TextView = itemView.findViewById(R.id.severityBadge)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)

        fun bind(alert: Alert) {
            // Set alert type (capitalize first letter)
            alertType.text = alert.type.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }

            // Set alert message
            alertMessage.text = alert.message

            // Set relative time
            alertTime.text = getRelativeTime(alert.timestamp)

            // Set severity badge
            severityBadge.text = alert.severity.uppercase()
            when (alert.severity.lowercase()) {
                "critical" -> {
                    severityBadge.setBackgroundColor(Color.parseColor("#EF4444"))
                    alertIconCard.setCardBackgroundColor(Color.parseColor("#FEE2E2"))
                    alertIcon.setColorFilter(Color.parseColor("#EF4444"))
                    alertIcon.setImageResource(R.drawable.ic_error)
                }
                "warning" -> {
                    severityBadge.setBackgroundColor(Color.parseColor("#F59E0B"))
                    alertIconCard.setCardBackgroundColor(Color.parseColor("#FEF3C7"))
                    alertIcon.setColorFilter(Color.parseColor("#F59E0B"))
                    alertIcon.setImageResource(R.drawable.ic_alert)
                }
                else -> { // info
                    severityBadge.setBackgroundColor(Color.parseColor("#3B82F6"))
                    alertIconCard.setCardBackgroundColor(Color.parseColor("#DBEAFE"))
                    alertIcon.setColorFilter(Color.parseColor("#3B82F6"))
                    alertIcon.setImageResource(R.drawable.ic_info)
                }
            }

            // Show unread indicator
            unreadIndicator.visibility = if (!alert.isRead) View.VISIBLE else View.GONE
        }

        private fun getRelativeTime(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(timestamp) ?: return timestamp
                val now = System.currentTimeMillis()
                val diff = now - date.time

                when {
                    diff < 60_000 -> "Just now"
                    diff < 3600_000 -> "${diff / 60_000}m ago"
                    diff < 86400_000 -> "${diff / 3600_000}h ago"
                    diff < 604800_000 -> "${diff / 86400_000}d ago"
                    else -> {
                        val displayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                        displayFormat.format(date)
                    }
                }
            } catch (e: Exception) {
                timestamp
            }
        }
    }
}
